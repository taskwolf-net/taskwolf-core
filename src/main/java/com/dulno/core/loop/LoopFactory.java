package com.dulno.core.loop;

import com.dulno.core.action.ActionExecutor;
import com.dulno.core.condition.Condition;
import com.dulno.core.loop.type.NumberLoop;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;

import java.util.Map;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class LoopFactory {
  public Loop create(
    String type, String content, Map<Integer, ActionExecutor> actions,
    Multimap<Integer, Condition> conditions
  ) {
    var json = new JSONObject(content);
    if (type.equals("loop-number")) {
      return NumberLoop.of(actions, conditions, json);
    }
    return null;
  }
}