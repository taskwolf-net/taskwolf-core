package net.taskwolf.core.workflow;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.action.ActionExecutor;
import net.taskwolf.core.action.ActionResult;
import net.taskwolf.core.bundle.Bundle;
import net.taskwolf.core.bundle.BundleDatabaseTable;
import net.taskwolf.core.condition.Condition;
import net.taskwolf.core.locale.Translation;
import net.taskwolf.core.mail.Mail;
import net.taskwolf.core.notification.NotificationDatabaseTable;
import net.taskwolf.core.notification.NotificationSetting;
import net.taskwolf.core.organization.Organization;
import net.taskwolf.core.organization.OrganizationDatabaseTable;
import net.taskwolf.core.organization.team.Team;
import net.taskwolf.core.organization.team.TeamDatabaseTable;
import net.taskwolf.core.user.User;
import net.taskwolf.core.user.UserDatabaseTable;
import net.taskwolf.core.workflow.notification.WorkflowFailureNotification;
import net.taskwolf.core.workflow.operation.Operation;
import net.taskwolf.core.workflow.operation.OperationDatabaseTable;
import net.taskwolf.core.workflow.throttle.WorkflowThrottle;
import net.taskwolf.core.workflow.throttle.WorkflowThrottleDatabaseTable;
import net.taskwolf.core.workflow.timeline.TimelineDatabaseTable;
import org.json.JSONObject;

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
  private final Translation translation;
  private final Mail notificationMail;
  private final WorkflowEntry workflowEntry;
  private final Map<Integer, ActionExecutor> actions;
  private final Multimap<Integer, Condition> conditions;
  private UUID bundleOwner;
  private int currentActionIndex = 0;

  /**
   * Triggers the workflow
   * @param information The information provided by the trigger
   */
  public CompletableFuture<Boolean> trigger(Map<String, Object> information) {
    return findWorkflowBundleOwner().thenAccept(owner -> bundleOwner = owner)
      .thenCompose(value -> checkOperationLimit().thenCompose(limitReached ->
        triggerLimit(information, limitReached)));
  }

  private CompletableFuture<Boolean> triggerLimit(
    Map<String, Object> information, boolean limitReached
  ) {
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
    return executeNextAction(Maps.newHashMap(information));
  }

  private CompletableFuture<Boolean> checkOperationLimit() {
    return bundleDatabaseTable.findBundle(bundleOwner).thenCompose(
      bundle -> operationDatabaseTable.findOperations(bundleOwner)
        .thenApply(operations -> checkOperationLimit(bundle, operations)));
  }

  private boolean checkOperationLimit(Bundle bundle, Operation operations) {
    if (System.currentTimeMillis() > operations.expiration()) {
      operationDatabaseTable.extendExpiration(bundleOwner);
      return true;
    }
    return operations.operations() + actions.size() >
      bundle.workflowOperationLimit();
  }

  private CompletableFuture<Boolean> executeNextAction(
    Map<String, Object> information
  ) {
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
