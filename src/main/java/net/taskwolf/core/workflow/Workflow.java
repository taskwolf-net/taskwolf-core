package net.taskwolf.core.workflow;

import com.google.common.collect.Multimap;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.action.Action;
import net.taskwolf.core.action.ActionResult;
import net.taskwolf.core.condition.Condition;
import net.taskwolf.core.locale.Locale;
import net.taskwolf.core.mail.TaskwolfMail;
import net.taskwolf.core.notification.NotificationDatabaseTable;
import net.taskwolf.core.notification.NotificationSetting;
import net.taskwolf.core.organization.OrganizationDatabaseTable;
import net.taskwolf.core.user.User;
import net.taskwolf.core.user.UserDatabaseTable;
import net.taskwolf.core.workflow.notification.WorkflowFailureNotification;
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
  private final OrganizationDatabaseTable organizationDatabaseTable;
  private final NotificationDatabaseTable notificationDatabaseTable;
  private final Locale englishLocale;
  private final TaskwolfMail notificationMail;
  private final WorkflowEntry workflowEntry;
  private final Map<Integer, Action> actions;
  private final Multimap<Integer, Condition> conditions;
  private int currentActionIndex = 0;

  public void trigger(Map<String, Object> information) {
    executeNextAction(information);
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
  }

  private CompletableFuture<User> findNotificationTarget() {
    if (workflowEntry.affiliation().isPrivate()) {
      return userDatabaseTable.findUser(workflowEntry.ownerId());
    }
    var futureResponse = new CompletableFuture<User>();
    organizationDatabaseTable.findOrganization(workflowEntry.ownerId())
      .thenAccept(organization -> userDatabaseTable.findUser(organization.owner())
        .thenAccept(futureResponse::complete));
    return futureResponse;
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
