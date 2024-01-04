package net.taskwolf.core.condition.text;

import net.taskwolf.core.condition.Condition;
import net.taskwolf.core.condition.ConditionDataType;
import net.taskwolf.core.condition.ConditionInformation;

import java.util.Map;

public final class ConditionTextEquals extends Condition {
  public static ConditionInformation information() {
    return ConditionInformation.builder()
      .withName("condition.text.equals")
      .withDataType(ConditionDataType.TEXT)
      .withIdentifier("condition-text-equals").build();
  }

  public static ConditionTextEquals create(String inputValue, String comparativeValue) {
    return new ConditionTextEquals(inputValue, comparativeValue);
  }

  private ConditionTextEquals(String inputValue, String comparativeValue) {
    super(inputValue, comparativeValue);
  }

  @Override
  public boolean compare(Map<String, Object> information) {
    dissolve(information);
    return inputValue().equals(comparativeValue());
  }
}
