package com.dulno.core.loop;

import com.dulno.core.action.ActionExecutor;
import com.dulno.core.action.ActionResult;
import com.dulno.core.condition.Condition;
import com.dulno.core.maintenance.MaintenanceSchedule;
import com.dulno.core.workflow.operation.OperationDatabaseTable;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Accessors(fluent = true)
@Getter(AccessLevel.PROTECTED)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Loop {
  private final OperationDatabaseTable operationDatabaseTable;
  private final MaintenanceSchedule maintenanceSchedule;
  private final Map<Integer, ActionExecutor> actions;
  private final Multimap<Integer, Condition> conditions;

  /**
   * Performs the actual looping
   * @param information The information that can be used for the loop
   * @return The future result of the loop
   */
  public abstract CompletableFuture<LoopResult> loop(Map<String, Object> information);

  /**
   * Performs a single iteration
   * @param information The information for that specific iteration
   * @return The result of this iteration
   */
  protected CompletableFuture<LoopResult> iterate(Map<String, Object> information) {
    return executeNextAction(0, information);
  }

  private CompletableFuture<LoopResult> executeNextAction(
    int currentActionIndex, Map<String, Object> information
  ) {
    //TODO: CHECK AND IMPLEMENT OPERATION LIMIT
    if (maintenanceSchedule.isMaintenanceRunning()) {
      return CompletableFuture.completedFuture(LoopResult.success());
    }
    if (currentActionIndex >= actions.size()) {
      return CompletableFuture.completedFuture(LoopResult.success());
    }
    var conditionResult = checkConditions(currentActionIndex, information);
    if (conditionResult.isPresent()) {
      return CompletableFuture.completedFuture(conditionResult.get());
    }
    var action = actions.get(currentActionIndex);
    return action.execute(information).thenCompose(result ->
      processActionResult(currentActionIndex, result, information));
  }

  private CompletableFuture<LoopResult> processActionResult(
    int currentActionIndex, ActionResult result, Map<String, Object> information
  ) {
    if (result.isFailure()) {
      return CompletableFuture.completedFuture(
        LoopResult.failure(result.failureMessage()));
    }
    information.putAll(result.information());
    return executeNextAction(currentActionIndex + 1, information);
  }

  private Optional<LoopResult> checkConditions(
    int index, Map<String, Object> information
  ) {
    if (!conditions.containsKey(index)) {
      return Optional.empty();
    }
    var allFulfilled = true;
    for (var condition : conditions.get(index)) {
      var result = condition.compare(information);
      if (result.isFailure()) {
        return Optional.of(LoopResult.failure(result.failureMessage()));
      }
      if (!result.comparisonResult()) {
        allFulfilled = false;
        break;
      }
    }
    return allFulfilled ? Optional.empty() : Optional.of(LoopResult.success());
  }
}
