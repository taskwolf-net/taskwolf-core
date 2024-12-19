package com.dulno.core.loop.type;

import com.beust.jcommander.internal.Lists;
import com.dulno.core.CoreModule;
import com.dulno.core.action.Action;
import com.dulno.core.bundle.Bundle;
import com.dulno.core.loop.Loop;
import com.dulno.core.loop.LoopInformation;
import com.dulno.core.maintenance.MaintenanceSchedule;
import com.dulno.core.trigger.Trigger;
import com.dulno.core.workflow.component.ComponentInformation;
import com.dulno.core.workflow.component.ComponentVariable;
import com.dulno.core.workflow.component.input.InputComponentDataType;
import com.dulno.core.workflow.component.input.InputComponentVariable;
import com.dulno.core.workflow.component.output.DynamicOutputComponentVariable;
import com.dulno.core.workflow.component.output.ListOutputComponentVariable;
import com.dulno.core.workflow.component.output.OutputComponentVariable;
import com.dulno.core.workflow.operation.OperationDatabaseTable;
import com.dulno.core.workflow.placeholder.PlaceholderDissolve;
import com.dulno.core.workflow.step.WorkflowStep;
import com.dulno.core.workflow.step.WorkflowStepResult;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public final class ItemLoop extends Loop {
  public static LoopInformation information(CoreModule coreModule) {
    return LoopInformation.builder()
      .withName("loop.item.name")
      .withDescription("loop.item.description")
      .withIdentifier("loop-item")
      .withInputVariable(InputComponentVariable.createRequired("loop.item.input.list.name",
        "loopList", "loop.item.input.list.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createOptional("loop.item.input.limit.name",
        "loopLimit", "loop.item.input.limit.description", InputComponentDataType.TEXT))
      .withOutputVariable(DynamicOutputComponentVariable.create(input ->
        findListOutputs(input.currentContent(), input.previousActions(), coreModule)))
      .withOutputVariable(OutputComponentVariable.create("loop.item.output.index", "loopIndex"))
      .withOutputVariable(OutputComponentVariable.create("loop.item.output.iterations", "loopIterations"))
      .withOutputVariable(OutputComponentVariable.create("loop.item.output.list", "loopList"))
      .build();
  }

  private static List<OutputComponentVariable> findListOutputs(
    JSONObject loopContent, List<JSONObject> previousComponents,
    CoreModule coreModule
  ) {
    try {
      var loopList = loopContent.getString("loopList").trim();
      var percentageCount = loopList.length() - loopList.replace("%", "").length();
      if (percentageCount != 2 || !loopList.startsWith("%") || !loopList.endsWith("%")) {
        return Lists.newArrayList();
      }
      var loopListPlaceholder = loopList.replace("%", "");
      for (var i = 0; i < previousComponents.size(); i++) {
        var rawComponent = previousComponents.get(i);
        var listVariableOutputs = findListVariableOutputs(loopListPlaceholder,
          rawComponent, i == 0, coreModule);
        if (listVariableOutputs.isPresent()) {
          return listVariableOutputs.get();
        }
      }
      return Lists.newArrayList();
    } catch (Exception exception) {
      return Lists.newArrayList();
    }
  }

  private static Optional<List<OutputComponentVariable>> findListVariableOutputs(
    String loopListPlaceholder, JSONObject rawComponent, boolean isTrigger,
    CoreModule coreModule
  ) {
    var outputs = findComponentOutputs(rawComponent, isTrigger, coreModule);
    Predicate<ComponentVariable> outputCondition =
      output -> output.identifier().equals(loopListPlaceholder);
    if (outputs.stream().noneMatch(outputCondition)) {
      return Optional.empty();
    }
    var variable = outputs.stream().filter(outputCondition).findFirst().get();
    if (variable instanceof ListOutputComponentVariable listVariable) {
      return Optional.of(listVariable.list());
    }
    return Optional.empty();
  }

  private static List<OutputComponentVariable> findComponentOutputs(
    JSONObject rawComponent, boolean isTrigger, CoreModule coreModule
  ) {
    Optional<ComponentInformation> information;
    if (isTrigger) {
      information = coreModule.findTrigger(rawComponent.getString("module"),
        rawComponent.getString("type")).map(Trigger::information);
    } else {
      information = coreModule.findAction(rawComponent.getString("module"),
        rawComponent.getString("type")).map(Action::information);
    }
    return information.map(ComponentInformation::outputVariables)
      .orElse(Lists.newArrayList());
  }

  public static ItemLoop of(
    OperationDatabaseTable operationDatabaseTable,
    MaintenanceSchedule maintenanceSchedule,
    Callable<CompletableFuture<List<WorkflowStep>>> stepGenerator, Bundle bundle,
    JSONObject content
  ) {
    return create(operationDatabaseTable, maintenanceSchedule, stepGenerator,
      bundle, content.getString("loopList"),
      content.has("loopLimit") ? Optional.of(content.getString("loopLimit")) :
        Optional.empty());
  }

  public static ItemLoop create(
    OperationDatabaseTable operationDatabaseTable,
    MaintenanceSchedule maintenanceSchedule,
    Callable<CompletableFuture<List<WorkflowStep>>> stepGenerator, Bundle bundle,
    String list, Optional<String> limit
  ) {
    return new ItemLoop(operationDatabaseTable, maintenanceSchedule, stepGenerator,
      bundle, list, limit);
  }

  private String list;
  private Optional<String> limit;

  private ItemLoop(
    OperationDatabaseTable operationDatabaseTable,
    MaintenanceSchedule maintenanceSchedule,
    Callable<CompletableFuture<List<WorkflowStep>>> stepGenerator, Bundle bundle,
    String list, Optional<String> limit
  ) {
    super(operationDatabaseTable, maintenanceSchedule, stepGenerator, bundle);
    this.list = list;
    this.limit = limit;
  }

  @Override
  public CompletableFuture<WorkflowStepResult> execute(
    Map<String, Object> information
  ) {
    try {
      var placeholderDissolve = PlaceholderDissolve.create(information);
      list = placeholderDissolve.dissolve(list);
      var list = new JSONArray(this.list).toList().stream()
        .map(value -> (Map<String, Object>) value).toList();
      if (limit.isPresent()) {
        limit = Optional.of(placeholderDissolve.dissolve(limit.get()));
      }
      var limit = this.limit.map(Integer::parseInt);
      return loopAsynchronously(list, limit, information);
    } catch (NumberFormatException exception) {
      return CompletableFuture.completedFuture(
        WorkflowStepResult.failure("loop.item.failure.wrong.format.limit"));
    } catch (Exception exception) {
      return CompletableFuture.completedFuture(
        WorkflowStepResult.failure("loop.item.failure.wrong.format.list"));
    }
  }

  private CompletableFuture<WorkflowStepResult> loopAsynchronously(
    List<Map<String, Object>> list, Optional<Integer> limit,
    Map<String, Object> information
  ) {
    var futureResponse = new CompletableFuture<WorkflowStepResult>();
    new Thread(() -> futureResponse.complete(
      loopSynchronously(list, limit, information))).start();
    return futureResponse;
  }

  private WorkflowStepResult loopSynchronously(
    List<Map<String, Object>> list, Optional<Integer> limit,
    Map<String, Object> information
  ) {
    var iterations = limit.map(value -> Math.min(list.size(), value))
      .orElseGet(list::size);
    for (var i = 0; i < iterations; i++) {
      var iterationInformation = createIterationInformation(list.get(i), i + 1,
        iterations, information);
      var iterationResult = iterate(iterationInformation).join();
      if (iterationResult.isFailure()) {
        return iterationResult;
      }
    }
    return WorkflowStepResult.success();
  }

  private Map<String, Object> createIterationInformation(
    Map<String, Object> item, int index, int iterations,
    Map<String, Object> information
  ) {
    information.putAll(item);
    information.put("loopIndex", index);
    information.put("loopIterations", iterations);
    information.put("loopList", list);
    return information;
  }
}