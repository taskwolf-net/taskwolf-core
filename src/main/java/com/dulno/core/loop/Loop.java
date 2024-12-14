package com.dulno.core.loop;

import com.dulno.core.action.ActionExecutor;
import com.dulno.core.condition.Condition;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

@Accessors(fluent = true)
@Getter(AccessLevel.PROTECTED)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Loop {
  private final Map<Integer, ActionExecutor> actions;
  private final Multimap<Integer, Condition> conditions;

  /**
   * Performs the actual looping
   * @param information The information that can be used for the loop
   * @return The result of the loop
   */
  public abstract LoopResult loop(Map<String, Object> information);
}
