package net.taskwolf.core.condition;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.workflow.placeholder.PlaceholderDissolve;

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

  public abstract ConditionResult compare(Map<String, Object> information);

  protected void dissolve(Map<String, Object> information) {
    var dissolve = PlaceholderDissolve.create(information);
    inputValue = dissolve.dissolve(inputValue);
    comparativeValue = dissolve.dissolve(comparativeValue);
  }
}
