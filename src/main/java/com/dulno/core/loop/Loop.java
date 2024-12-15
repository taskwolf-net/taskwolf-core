package com.dulno.core.loop;

import com.dulno.core.bundle.Bundle;
import com.dulno.core.maintenance.MaintenanceSchedule;
import com.dulno.core.workflow.operation.Operation;
import com.dulno.core.workflow.operation.OperationDatabaseTable;
import com.dulno.core.workflow.step.WorkflowStep;
import com.dulno.core.workflow.step.WorkflowStepResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Accessors(fluent = true)
@Getter(AccessLevel.PROTECTED)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Loop implements WorkflowStep {
  private final OperationDatabaseTable operationDatabaseTable;
  private final MaintenanceSchedule maintenanceSchedule;
  private final List<WorkflowStep> steps;
  private final Bundle bundle;

  /**
   * Performs the actual looping
   * @param information The information that can be used for the loop
   * @return The future result of the loop
   */
  public abstract CompletableFuture<WorkflowStepResult> execute(
    Map<String, Object> information);

  /**
   * Performs a single iteration
   * @param information The information for that specific iteration
   * @return The result of this iteration
   */
  protected CompletableFuture<WorkflowStepResult> iterate(
    Map<String, Object> information
  ) {
    return checkOperationLimit().thenCompose(limitReached ->
      executeNextStep(0, information, limitReached));
  }

  private CompletableFuture<WorkflowStepResult> executeNextStep(
    int currentIndex, Map<String, Object> information, boolean limitReached
  ) {
    if (maintenanceSchedule.isMaintenanceRunning()) {
      return CompletableFuture.completedFuture(WorkflowStepResult.success());
    }
    if (limitReached) {
      return CompletableFuture.completedFuture(
        WorkflowStepResult.failure("workflow.operations.limit.reached"));
    }
    if (currentIndex >= steps.size()) {
      return CompletableFuture.completedFuture(WorkflowStepResult.success());
    }
    var step = steps.get(currentIndex);
    return step.execute(information).thenCompose(result ->
      processStepResult(currentIndex, result, information));
  }

  private CompletableFuture<WorkflowStepResult> processStepResult(
    int currentIndex, WorkflowStepResult result, Map<String, Object> information
  ) {
    if (result.isFailure()) {
      return CompletableFuture.completedFuture(
        WorkflowStepResult.failure(result.failureMessage()));
    }
    if (!result.mayContinue()) {
      return CompletableFuture.completedFuture(WorkflowStepResult.success());
    }
    information.putAll(result.passOnInformation());
    return checkOperationLimit().thenCompose(limitReached ->
      executeNextStep(currentIndex + 1, information, limitReached));
  }

  private CompletableFuture<Boolean> checkOperationLimit() {
    return operationDatabaseTable.findOperations(bundle.ownerId())
      .thenCompose(this::checkOperationLimit);
  }

  private CompletableFuture<Boolean> checkOperationLimit(Operation operation) {
    if (operation.operations() + 1 > bundle.workflowOperationLimit()) {
      return CompletableFuture.completedFuture(true);
    }
    return operationDatabaseTable.addOperations(bundle.ownerId(), 1)
      .thenApply(value -> false);
  }
}
