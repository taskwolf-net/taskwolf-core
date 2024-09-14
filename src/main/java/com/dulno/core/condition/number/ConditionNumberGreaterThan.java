package com.dulno.core.condition.number;

import com.dulno.core.condition.Condition;
import com.dulno.core.condition.ConditionDataType;
import com.dulno.core.condition.ConditionInformation;
import com.dulno.core.condition.ConditionResult;

import java.util.Map;

public final class ConditionNumberGreaterThan extends Condition {
  public static ConditionInformation information() {
    return ConditionInformation.builder()
      .withName("condition.number.greater.than")
      .withDataType(ConditionDataType.NUMBER)
      .withIdentifier("condition-number-is-greater").build();
  }

  public static ConditionNumberGreaterThan create(String inputValue, String comparativeValue) {
    return new ConditionNumberGreaterThan(inputValue, comparativeValue);
  }

  private ConditionNumberGreaterThan(String inputValue, String comparativeValue) {
    super(inputValue, comparativeValue);
  }

  @Override
  public ConditionResult compare(Map<String, Object> information) {
    dissolve(information);
    if (!inputValue().matches("-?\\d+(\\.\\d+)?") || !comparativeValue().matches("-?\\d+(\\.\\d+)?")) {
      return ConditionResult.failure("condition.not.a.number");
    }
    return ConditionResult.success(Integer.parseInt(inputValue()) >
      Integer.parseInt(comparativeValue()));
  }
}
