package com.dulno.core.loop.type;

import com.dulno.core.action.ActionExecutor;
import com.dulno.core.condition.Condition;
import com.dulno.core.loop.Loop;
import com.dulno.core.loop.LoopInformation;
import com.dulno.core.loop.LoopResult;
import com.dulno.core.maintenance.MaintenanceSchedule;
import com.dulno.core.workflow.component.input.InputComponentDataType;
import com.dulno.core.workflow.component.input.InputComponentVariable;
import com.dulno.core.workflow.component.output.OutputComponentVariable;
import com.dulno.core.workflow.operation.OperationDatabaseTable;
import com.google.common.collect.Multimap;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class NumberLoop extends Loop {
  public static LoopInformation information() {
    return LoopInformation.builder()
      .withName("loop.number.name")
      .withDescription("loop.number.description")
      .withIdentifier("loop-number")
      .withInputVariable(InputComponentVariable.createRequired("loop.number.input.start.name",
        "loopStart", "loop.number.input.start.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createRequired("loop.number.input.end.name",
        "loopEnd", "loop.number.input.end.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createRequired("loop.number.input.limit.name",
        "loopLimit", "loop.number.input.limit.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("loop.number.output.index", "loopIndex"))
      .withOutputVariable(OutputComponentVariable.create("loop.number.output.start", "loopStart"))
      .withOutputVariable(OutputComponentVariable.create("loop.number.output.end", "loopEnd"))
      .build();
  }

  public static NumberLoop of(
    OperationDatabaseTable operationDatabaseTable,
    MaintenanceSchedule maintenanceSchedule, Map<Integer, ActionExecutor> actions,
    Multimap<Integer, Condition> conditions, JSONObject content
  ) {
    return create(operationDatabaseTable, maintenanceSchedule, actions,
      conditions, content.getInt("loopStart"), content.getInt("loopEnd"),
      content.getInt("loopLimit"));
  }

  public static NumberLoop create(
    OperationDatabaseTable operationDatabaseTable,
    MaintenanceSchedule maintenanceSchedule, Map<Integer, ActionExecutor> actions,
    Multimap<Integer, Condition> conditions, int start, int end, int limit
  ) {
    return new NumberLoop(operationDatabaseTable, maintenanceSchedule, actions,
      conditions, start, end, limit);
  }

  private final int start;
  private final int end;
  private final int limit;

  private NumberLoop(
    OperationDatabaseTable operationDatabaseTable,
    MaintenanceSchedule maintenanceSchedule, Map<Integer, ActionExecutor> actions,
    Multimap<Integer, Condition> conditions, int start, int end, int limit
  ) {
    super(operationDatabaseTable, maintenanceSchedule, actions, conditions);
    this.start = start;
    this.end = end;
    this.limit = limit;
  }

  @Override
  public CompletableFuture<LoopResult> loop(Map<String, Object> information) {
    var futureResponse = new CompletableFuture<LoopResult>();
    new Thread(() -> futureResponse.complete(loopSynchronously(information)))
      .start();
    return futureResponse;
  }

  private LoopResult loopSynchronously(Map<String, Object> information) {
    for (var i = start; i < Math.min(end, start + limit); i++) {
      var iterationInformation = createIterationInformation(i, information);
      var iterationResult = iterate(iterationInformation).join();
      if (iterationResult.isFailure()) {
        return iterationResult;
      }
    }
    return LoopResult.success();
  }

  private Map<String, Object> createIterationInformation(
    int index, Map<String, Object> information
  ) {
    information.put("loopIndex", index);
    information.put("loopStart", start);
    information.put("loopEnd", Math.min(end, start + limit));
    return information;
  }
}
