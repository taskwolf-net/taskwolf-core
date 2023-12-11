package net.taskwolf.core.condition.text;


import net.taskwolf.core.condition.Condition;
import net.taskwolf.core.condition.ConditionDataType;
import net.taskwolf.core.condition.ConditionInformation;

import java.util.Map;

public final class ConditionTextStartsWith extends Condition {
  public static ConditionInformation information() {
    return ConditionInformation.builder()
      .withName("Starts with")
      .withDataType(ConditionDataType.TEXT)
      .withIdentifier("condition-text-starts-with").build();
  }

  public static ConditionTextStartsWith create(String inputValue, String comparativeValue) {
    return new ConditionTextStartsWith(inputValue, comparativeValue);
  }

  private ConditionTextStartsWith(String inputValue, String comparativeValue) {
    super(inputValue, comparativeValue);
  }

  @Override
  public boolean compare(Map<String, Object> information) {
    dissolve(information);
    return inputValue().startsWith(comparativeValue());
  }
}
