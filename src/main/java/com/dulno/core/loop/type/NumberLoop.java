package com.dulno.core.loop.type;

import com.dulno.core.action.ActionExecutor;
import com.dulno.core.condition.Condition;
import com.dulno.core.loop.Loop;
import com.dulno.core.loop.LoopInformation;
import com.dulno.core.loop.LoopResult;
import com.dulno.core.maintenance.MaintenanceSchedule;
import com.dulno.core.workflow.operation.OperationDatabaseTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public final class NumberLoop extends Loop {
  public static LoopInformation information() {
    //TODO: ADD LOCALES AND SPECIFY VARIABLES
    return LoopInformation.builder()
      .withName("loop.number.name")
      .withDescription("loop.number.description")
      .withIdentifier("loop-number").build();
  }

  public static NumberLoop of(
    OperationDatabaseTable operationDatabaseTable,
    MaintenanceSchedule maintenanceSchedule, Map<Integer, ActionExecutor> actions,
    Multimap<Integer, Condition> conditions, JSONObject content
  ) {
    return create(operationDatabaseTable, maintenanceSchedule, actions,
      conditions, content.getInt("start"), content.getInt("end"),
      content.getInt("limit"));
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
    //IDEA: USE A EXECUTOR SERVICE?
    //IDEA: DO EVERYTHING SYNC AND DO IT INSIDE A THREAD?
    var executorService = Executors.newSingleThreadExecutor();
    for (var i = start; i < Math.min(limit, end); i++) {
      var iterationInformation = createIterationInformation(i, information);
      executorService.submit(() -> iterate(iterationInformation).join());
    }
    return null;
  }

  private Map<String, Object> createIterationInformation(
    int index, Map<String, Object> information
  ) {
    information.put("loopIndex", index);
    information.put("loopStart", start);
    information.put("loopEnd", Math.min(limit, end));
    return information;
  }
}
