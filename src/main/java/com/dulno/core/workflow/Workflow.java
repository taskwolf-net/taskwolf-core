package com.dulno.core.workflow;

import com.dulno.core.action.ActionExecutor;
import com.dulno.core.bundle.Bundle;
import com.dulno.core.error.ErrorRepository;
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
import com.dulno.core.workflow.step.WorkflowStep;
import com.dulno.core.workflow.step.WorkflowStepResult;
import com.dulno.core.workflow.throttle.WorkflowThrottleDatabaseTable;
import com.dulno.core.workflow.timeline.TimelineDatabaseTable;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import com.dulno.core.locale.Translation;
import com.dulno.core.workflow.throttle.WorkflowThrottle;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public final class Workflow {
  private final WorkflowDatabaseTable workflowDatabaseTable;
  private final TimelineDatabaseTable timelineDatabaseTable;
  private final UserDatabaseTable userDatabaseTable;
  private final OperationDatabaseTable operationDatabaseTable;
  private final WorkflowThrottleDatabaseTable workflowThrottleDatabaseTable;
  private final OrganizationDatabaseTable organizationDatabaseTable;
  private final NotificationDatabaseTable notificationDatabaseTable;
  private final MaintenanceSchedule maintenanceSchedule;
  private final Translation translation;
  private final ErrorRepository errorRepository;
  private final Mail notificationMail;
  private final WorkflowEntry workflowEntry;
  private final List<WorkflowStep> steps;
  private final Bundle bundle;
  private int currentStepIndex = 0;
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
    return checkOperationLimitExtension()
      .thenCompose(extensionValue -> triggerLimit(information))
      .exceptionally(this::processWorkflowException);
  }

  private CompletableFuture<Void> checkOperationLimitExtension() {
    return operationDatabaseTable.findOperations(bundle.ownerId())
      .thenCompose(this::checkOperationLimitExtension);
  }

  private CompletableFuture<Void> checkOperationLimitExtension(
    Operation operations
  ) {
    if (System.currentTimeMillis() > operations.expiration()) {
      return operationDatabaseTable.extendExpiration(bundle.ownerId());
    }
    return CompletableFuture.completedFuture(null);
  }

  private Boolean processWorkflowException(Throwable throwable) {
    errorRepository.processError(throwable);
    return false;
  }

  private CompletableFuture<Boolean> triggerLimit(
    Map<String, Object> information
  ) {
    if (System.currentTimeMillis() > bundle.expiration()) {
      return CompletableFuture.completedFuture(false);
    }
    return WorkflowThrottle.create(workflowThrottleDatabaseTable, bundle.ownerId())
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
    return checkOperationLimit(false).thenCompose(limitReached ->
      executeNextStep(triggerInformation, limitReached));
  }

  private CompletableFuture<Boolean> executeNextStep(
    Map<String, Object> information, boolean limitReached
  ) {
    if (maintenanceSchedule.isMaintenanceRunning() || limitReached) {
      return CompletableFuture.completedFuture(false);
    }
    if (currentStepIndex >= steps.size()) {
      postExecutionSuccess();
      return CompletableFuture.completedFuture(true);
    }
    var step = steps.get(currentStepIndex);
    currentStepIndex++;
    return step.execute(information)
      .thenCompose(result -> checkOperationLimit(step)
        .thenCompose(newLimitReached -> processStepResult(result, information,
          newLimitReached)));
  }

  private CompletableFuture<Boolean> processStepResult(
    WorkflowStepResult result, Map<String, Object> information,
    boolean limitReached
  ) {
    if (result.isFailure()) {
      postExecutionFailure(result.failureMessage());
      return CompletableFuture.completedFuture(false);
    }
    if (!result.mayContinue()) {
      postExecutionSuccess();
      return CompletableFuture.completedFuture(true);
    }
    information.putAll(result.passOnInformation());
    return executeNextStep(information, limitReached);
  }

  private CompletableFuture<Boolean> checkOperationLimit(WorkflowStep step) {
    if (!(step instanceof ActionExecutor)) {
      return CompletableFuture.completedFuture(false);
    }
    return checkOperationLimit(true);
  }

  private CompletableFuture<Boolean> checkOperationLimit(boolean addOperation) {
    return operationDatabaseTable.findOperations(bundle.ownerId())
      .thenCompose(operation -> checkOperationLimit(operation, addOperation));
  }

  private CompletableFuture<Boolean> checkOperationLimit(
    Operation operation, boolean addOperation
  ) {
    if (operation.operations() + 1 > bundle.workflowOperationLimit()) {
      postExecutionFailure("workflow.operations.limit.reached");
      return CompletableFuture.completedFuture(true);
    }
    if (!addOperation) {
      return CompletableFuture.completedFuture(false);
    }
    return operationDatabaseTable.addOperations(bundle.ownerId(), 1)
      .thenApply(value -> false);
  }

  private void postExecutionSuccess() {
    long currentTime = System.currentTimeMillis();
    if (workflowEntry.state().isFailing()) {
      workflowDatabaseTable.updateWorkflowState(workflowEntry, WorkflowState.OPERATIONAL);
    }
    timelineDatabaseTable.generateAvailableEntryId().thenAccept(id ->
      timelineDatabaseTable.insertEntry(id, workflowEntry.id(), currentTime,
        "timeline-workflow-execute", "{}"));
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
    return userDatabaseTable.userExists(bundle.ownerId())
      .thenCompose(exists -> exists ?
        CompletableFuture.completedFuture(bundle.ownerId()) :
        organizationDatabaseTable.findOrganization(bundle.ownerId())
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
}
