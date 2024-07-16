package net.taskwolf.core.workflow;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.action.ActionExecutor;
import net.taskwolf.core.action.ActionResult;
import net.taskwolf.core.bundle.Bundle;
import net.taskwolf.core.bundle.BundleDatabaseTable;
import net.taskwolf.core.condition.Condition;
import net.taskwolf.core.locale.Locale;
import net.taskwolf.core.mail.TaskwolfMail;
import net.taskwolf.core.notification.NotificationDatabaseTable;
import net.taskwolf.core.notification.NotificationSetting;
import net.taskwolf.core.organization.Organization;
import net.taskwolf.core.organization.OrganizationDatabaseTable;
import net.taskwolf.core.user.User;
import net.taskwolf.core.user.UserDatabaseTable;
import net.taskwolf.core.workflow.notification.WorkflowFailureNotification;
import net.taskwolf.core.workflow.operation.Operation;
import net.taskwolf.core.workflow.operation.OperationDatabaseTable;
import net.taskwolf.core.workflow.timeline.TimelineDatabaseTable;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public final class Workflow {
  private final WorkflowDatabaseTable workflowDatabaseTable;
  private final WorkflowExecutionDatabaseTable workflowExecutionDatabaseTable;
  private final TimelineDatabaseTable timelineDatabaseTable;
  private final UserDatabaseTable userDatabaseTable;
  private final BundleDatabaseTable bundleDatabaseTable;
  private final OperationDatabaseTable operationDatabaseTable;
  private final OrganizationDatabaseTable organizationDatabaseTable;
  private final NotificationDatabaseTable notificationDatabaseTable;
  private final Locale englishLocale;
  private final TaskwolfMail notificationMail;
  private final WorkflowEntry workflowEntry;
  private final Map<Integer, ActionExecutor> actions;
  private final Multimap<Integer, Condition> conditions;
  private int currentActionIndex = 0;

  /**
   * Triggers the workflow
   * @param information The information provided by the trigger
   */
  public void trigger(Map<String, Object> information) {
    checkOperationLimit().thenAccept(limitReached ->
      trigger(information, limitReached));
  }

  private void trigger(Map<String, Object> information, boolean limitReached) {
    if (limitReached) {
      postExecutionFailure("workflow.operations.limit.reached");
      return;
    }
    executeNextAction(Maps.newHashMap(information));
  }

  private CompletableFuture<Boolean> checkOperationLimit() {
    return bundleDatabaseTable.findBundle(workflowEntry.ownerId()).thenCompose(
      bundle -> operationDatabaseTable.findOperations(workflowEntry.ownerId())
        .thenApply(operations -> checkOperationLimit(bundle, operations)));
  }

  private boolean checkOperationLimit(Bundle bundle, Operation operations) {
    if (System.currentTimeMillis() > operations.expiration()) {
      operationDatabaseTable.extendExpiration(operations);
      return true;
    }
    return operations.operations() + actions.size() >
      bundle.workflowOperationLimit();
  }

  private void executeNextAction(Map<String, Object> information) {
    if (currentActionIndex >= actions.size()) {
      postExecutionSuccess();
      return;
    }
    if (!checkConditions(currentActionIndex, information)) {
      return;
    }
    var action = actions.get(currentActionIndex);
    currentActionIndex++;
    action.execute(information).thenAccept(result ->
      processActionResult(result, information));
  }

  private void processActionResult(
    ActionResult result, Map<String, Object> information
  ) {
    if (result.isFailure()) {
      postExecutionFailure(result.failureMessage());
      return;
    }
    information.putAll(result.information());
    executeNextAction(information);
  }

  private void postExecutionSuccess() {
    long currentTime = System.currentTimeMillis();
    if (workflowEntry.state().isFailing()) {
      workflowDatabaseTable.updateWorkflowState(workflowEntry, WorkflowState.OPERATIONAL);
    }
    workflowExecutionDatabaseTable.addWorkflowExecution(workflowEntry.id(), currentTime);
    timelineDatabaseTable.generateAvailableEntryId().thenAccept(id ->
      timelineDatabaseTable.insertEntry(id, workflowEntry.id(), currentTime,
        "timeline-workflow-execute", "{}"));
    operationDatabaseTable.addOperations(workflowEntry.ownerId(), actions.size());
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
      operationDatabaseTable.addOperations(workflowEntry.ownerId(),
        currentActionIndex);
    }
  }

  private CompletableFuture<User> findNotificationTarget() {
    return userDatabaseTable.userExists(workflowEntry.ownerId())
      .thenCompose(exists -> exists ?
        CompletableFuture.completedFuture(workflowEntry.ownerId()) :
        organizationDatabaseTable.findOrganization(workflowEntry.ownerId())
          .thenApply(Organization::owner))
      .thenCompose(userDatabaseTable::findUser);
  }

  private void sendExecutionFailureNotification(
    User target, NotificationSetting notificationSetting, String failureMessage
  ) {
    if (!notificationSetting.general() || !notificationSetting.workflowFail()) {
      return;
    }
    WorkflowFailureNotification.create(notificationMail, target.email(),
      englishLocale.findText(failureMessage)).send();
  }
}
