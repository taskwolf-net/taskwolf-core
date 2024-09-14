package com.dulno.core.condition;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.workflow.placeholder.PlaceholderDissolve;

import java.util.Map;

@Accessors(fluent = true)
@Getter(AccessLevel.PROTECTED)
public abstract class Condition {
  private String inputValue;
  private String comparativeValue;

  protected Condition(String inputValue, String comparativeValue) {
    this.inputValue = inputValue;
    this.comparativeValue = comparativeValue;
  }

  /**
   * Performs the actual comparison
   * @param information The information that can be used for comparison
   * @return The result of the comparison
   */
  public abstract ConditionResult compare(Map<String, Object> information);

  /**
   * Is used to dissolve the inputValue and comparativeValue
   * @param information The information that is used for dissolution
   */
  protected void dissolve(Map<String, Object> information) {
    var dissolve = PlaceholderDissolve.create(information);
    inputValue = dissolve.dissolve(inputValue);
    comparativeValue = dissolve.dissolve(comparativeValue);
  }
}
