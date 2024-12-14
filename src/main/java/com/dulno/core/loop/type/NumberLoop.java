package com.dulno.core.loop.type;

import com.dulno.core.action.ActionExecutor;
import com.dulno.core.condition.Condition;
import com.dulno.core.loop.Loop;
import com.dulno.core.loop.LoopInformation;
import com.dulno.core.loop.LoopResult;
import com.google.common.collect.Multimap;
import org.json.JSONObject;

import java.util.Map;

public final class NumberLoop extends Loop {
  public static LoopInformation information() {
    return LoopInformation.builder()
      .withName("loop.number.name")
      .withDescription("loop.number.description")
      .withIdentifier("loop-number").build();
  }

  public static NumberLoop of(
    Map<Integer, ActionExecutor> actions, Multimap<Integer, Condition> conditions,
    JSONObject content
  ) {
    return new NumberLoop(actions, conditions, content.getInt("start"),
      content.getInt("end"), content.getInt("limit"));
  }

  public static NumberLoop create(
    Map<Integer, ActionExecutor> actions, Multimap<Integer, Condition> conditions,
    int start, int end, int limit
  ) {
    return new NumberLoop(actions, conditions, start, end, limit);
  }

  private final int start;
  private final int end;
  private final int limit;

  private NumberLoop(
    Map<Integer, ActionExecutor> actions, Multimap<Integer, Condition> conditions,
    int start, int end, int limit
  ) {
    super(actions, conditions);
    this.start = start;
    this.end = end;
    this.limit = limit;
  }

  @Override
  public LoopResult loop(Map<String, Object> information) {
    return null;
  }
}
