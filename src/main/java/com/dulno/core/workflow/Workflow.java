package com.dulno.core.workflow;

import com.dulno.core.action.ActionExecutor;
import com.dulno.core.action.ActionResult;
import com.dulno.core.bundle.Bundle;
import com.dulno.core.bundle.BundleDatabaseTable;
import com.dulno.core.condition.Condition;
import com.dulno.core.mail.Mail;
import com.dulno.core.maintenance.MaintenanceSchedule;
import com.dulno.core.notification.NotificationDatabaseTable;
import com.dulno.core.notification.NotificationSetting;
import com.dulno.core.organization.Organization;
import com.dulno.core.organization.OrganizationDatabaseTable;
import com.dulno.core.user.User;
import com.dulno.core.user.UserDatabaseTable;
import com.dulno.core.workflow.notification.WorkflowFailureNotification;
import com.dulno.core.workflow.operation.Operation;
import com.dulno.core.workflow.operation.OperationDatabaseTable;
import com.dulno.core.workflow.throttle.WorkflowThrottleDatabaseTable;
import com.dulno.core.workflow.timeline.TimelineDatabaseTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import lombok.RequiredArgsConstructor;
import com.dulno.core.locale.Translation;
import com.dulno.core.organization.team.Team;
import com.dulno.core.organization.team.TeamDatabaseTable;
import com.dulno.core.workflow.throttle.WorkflowThrottle;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public final class Workflow {
  private final WorkflowDatabaseTable workflowDatabaseTable;
  private final TimelineDatabaseTable timelineDatabaseTable;
  private final UserDatabaseTable userDatabaseTable;
  private final BundleDatabaseTable bundleDatabaseTable;
  private final OperationDatabaseTable operationDatabaseTable;
  private final WorkflowThrottleDatabaseTable workflowThrottleDatabaseTable;
  private final OrganizationDatabaseTable organizationDatabaseTable;
  private final TeamDatabaseTable teamDatabaseTable;
  private final NotificationDatabaseTable notificationDatabaseTable;
  private final MaintenanceSchedule maintenanceSchedule;
  private final Translation translation;
  private final Mail notificationMail;
  private final WorkflowEntry workflowEntry;
  private final Map<Integer, ActionExecutor> actions;
  private final Multimap<Integer, Condition> conditions;
  private UUID bundleOwner;
  private int currentActionIndex = 0;
  private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
  private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");


  /**
   * Triggers the workflow
   * @param information The information provided by the trigger
   */
  public CompletableFuture<Boolean> trigger(Map<String, Object> information) {
    if (maintenanceSchedule.isMaintenanceRunning()) {
      return CompletableFuture.completedFuture(false);
    }
    return findWorkflowBundleOwner().thenAccept(owner -> bundleOwner = owner)
      .thenCompose(value -> bundleDatabaseTable.findBundle(bundleOwner)
        .thenCompose(bundle -> checkOperationLimit(bundle)
          .thenCompose(limitReached -> triggerLimit(information, bundle,
            limitReached))));
  }

  private CompletableFuture<Boolean> triggerLimit(
    Map<String, Object> information, Bundle bundle, boolean limitReached
  ) {
    if (System.currentTimeMillis() > bundle.expiration()) {
      return CompletableFuture.completedFuture(false);
    }
    if (limitReached) {
      postExecutionFailure("workflow.operations.limit.reached");
      return CompletableFuture.completedFuture(false);
    }
    return WorkflowThrottle.create(workflowThrottleDatabaseTable, bundleOwner)
      .registerWorkflowExecution().thenCompose(throttleAllowsExecution ->
        triggerThrottle(information, throttleAllowsExecution));
  }

  private CompletableFuture<Boolean> triggerThrottle(
    Map<String, Object> information, boolean throttleAllowsExecution
  ) {
    if (!throttleAllowsExecution) {
      postExecutionFailure("workflow.throttle.intervention");
      return CompletableFuture.completedFuture(false);
    }
    var triggerInformation = Maps.newHashMap(information);
    var time = System.currentTimeMillis();
    triggerInformation.put("formattedTime", timeFormat.format(new Date(time)));
    triggerInformation.put("formattedDate", dateFormat.format(time));
    triggerInformation.put("unixTime", time);
    return executeNextAction(triggerInformation);
  }

  private CompletableFuture<Boolean> checkOperationLimit(Bundle bundle) {
    return operationDatabaseTable.findOperations(bundleOwner)
      .thenApply(operations -> checkOperationLimit(bundle, operations));
  }

  private boolean checkOperationLimit(Bundle bundle, Operation operations) {
    if (System.currentTimeMillis() > operations.expiration()) {
      operationDatabaseTable.extendExpiration(bundleOwner);
      return false;
    }
    return operations.operations() + actions.size() >
      bundle.workflowOperationLimit();
  }

  private CompletableFuture<Boolean> executeNextAction(
    Map<String, Object> information
  ) {
    if (maintenanceSchedule.isMaintenanceRunning()) {
      return CompletableFuture.completedFuture(false);
    }
    if (currentActionIndex >= actions.size()) {
      postExecutionSuccess();
      return CompletableFuture.completedFuture(true);
    }
    if (!checkConditions(currentActionIndex, information)) {
      return CompletableFuture.completedFuture(true);
    }
    var action = actions.get(currentActionIndex);
    currentActionIndex++;
    return action.execute(information).thenCompose(result ->
      processActionResult(result, information));
  }

  private CompletableFuture<Boolean> processActionResult(
    ActionResult result, Map<String, Object> information
  ) {
    if (result.isFailure()) {
      postExecutionFailure(result.failureMessage());
      return CompletableFuture.completedFuture(false);
    }
    information.putAll(result.information());
    return executeNextAction(information);
  }

  private void postExecutionSuccess() {
    long currentTime = System.currentTimeMillis();
    if (workflowEntry.state().isFailing()) {
      workflowDatabaseTable.updateWorkflowState(workflowEntry, WorkflowState.OPERATIONAL);
    }
    timelineDatabaseTable.generateAvailableEntryId().thenAccept(id ->
      timelineDatabaseTable.insertEntry(id, workflowEntry.id(), currentTime,
        "timeline-workflow-execute", "{}"));
    operationDatabaseTable.addOperations(bundleOwner, actions.size());
  }

  private boolean checkConditions(int index, Map<String, Object> information) {
    if (!conditions.containsKey(index)) {
      return true;
    }
    var allFulfilled = true;
    for (var condition : conditions.get(index)) {
      var result = condition.compare(information);
      if (result.isFailure()) {
        postExecutionFailure(result.failureMessage());
        return false;
      }
      if (!result.comparisonResult()) {
        allFulfilled = false;
        break;
      }
    }
    return allFulfilled;
  }

  private void postExecutionFailure(String failureMessage) {
    long currentTime = System.currentTimeMillis();
    if (workflowEntry.state().isOperational()) {
      workflowDatabaseTable.updateWorkflowState(workflowEntry, WorkflowState.FAILING);
    }
    timelineDatabaseTable.generateAvailableEntryId().thenAccept(id ->
      timelineDatabaseTable.insertEntry(id, workflowEntry.id(), currentTime,
        "timeline-workflow-failure", new JSONObject(Map.of("message",
          failureMessage)).toString()));
    findNotificationTarget().thenAccept(target -> notificationDatabaseTable
      .findNotificationSettings(target.id()).thenAccept(setting ->
        sendExecutionFailureNotification(target, setting, failureMessage)));
    if (currentActionIndex > 0) {
      operationDatabaseTable.addOperations(bundleOwner, currentActionIndex);
    }
  }

  private CompletableFuture<User> findNotificationTarget() {
    return userDatabaseTable.userExists(bundleOwner)
      .thenCompose(exists -> exists ?
        CompletableFuture.completedFuture(bundleOwner) :
        organizationDatabaseTable.findOrganization(bundleOwner)
          .thenApply(Organization::owner))
      .thenCompose(userDatabaseTable::findUser);
  }

  private void sendExecutionFailureNotification(
    User target, NotificationSetting notificationSetting, String failureMessage
  ) {
    if (!notificationSetting.general() || !notificationSetting.workflowFail()) {
      return;
    }
    WorkflowFailureNotification.create(translation, notificationMail, target,
      failureMessage).send();
  }

  private CompletableFuture<UUID> findWorkflowBundleOwner() {
    var owner = workflowEntry.ownerId();
    return userDatabaseTable.userExists(owner)
      .thenCompose(userExists -> organizationDatabaseTable.organizationExists(owner)
        .thenCompose(organizationExists -> userExists || organizationExists ?
          CompletableFuture.completedFuture(owner) :
          teamDatabaseTable.findTeam(owner).thenApply(Team::organizationId)));
  }
}
