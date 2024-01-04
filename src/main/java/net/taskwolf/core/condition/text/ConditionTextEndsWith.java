package net.taskwolf.core.condition.text;

import net.taskwolf.core.condition.Condition;
import net.taskwolf.core.condition.ConditionDataType;
import net.taskwolf.core.condition.ConditionInformation;

import java.util.Map;

public final class ConditionTextEndsWith extends Condition {
  public static ConditionInformation information() {
    return ConditionInformation.builder()
      .withName("condition.text.ends.with")
      .withDataType(ConditionDataType.TEXT)
      .withIdentifier("condition-text-ends-with").build();
  }

  public static ConditionTextEndsWith create(String inputValue, String comparativeValue) {
    return new ConditionTextEndsWith(inputValue, comparativeValue);
  }

  private ConditionTextEndsWith(String inputValue, String comparativeValue) {
    super(inputValue, comparativeValue);
  }

  @Override
  public boolean compare(Map<String, Object> information) {
    dissolve(information);
    return inputValue().endsWith(comparativeValue());
  }
}
