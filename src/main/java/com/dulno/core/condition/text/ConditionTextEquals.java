package com.dulno.core.condition.text;

import com.dulno.core.condition.Condition;
import com.dulno.core.condition.ConditionDataType;
import com.dulno.core.condition.ConditionInformation;
import com.dulno.core.condition.ConditionResult;

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
  public ConditionResult compare(Map<String, Object> information) {
    dissolve(information);
    return ConditionResult.success(inputValue().equals(comparativeValue()));
  }
}
