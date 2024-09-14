package com.dulno.core.condition.text;

import com.dulno.core.condition.Condition;
import com.dulno.core.condition.ConditionDataType;
import com.dulno.core.condition.ConditionInformation;
import com.dulno.core.condition.ConditionResult;

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
  public ConditionResult compare(Map<String, Object> information) {
    dissolve(information);
    return ConditionResult.success(inputValue().endsWith(comparativeValue()));
  }
}
