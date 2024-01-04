package net.taskwolf.core.condition.number;

import net.taskwolf.core.condition.Condition;
import net.taskwolf.core.condition.ConditionDataType;
import net.taskwolf.core.condition.ConditionInformation;
import net.taskwolf.core.condition.ConditionResult;

import java.util.Map;

public final class ConditionNumberSmallerThan extends Condition {
  public static ConditionInformation information() {
    return ConditionInformation.builder()
      .withName("condition.number.smaller.than")
      .withDataType(ConditionDataType.NUMBER)
      .withIdentifier("condition-number-is-smaller").build();
  }

  public static ConditionNumberSmallerThan create(String inputValue, String comparativeValue) {
    return new ConditionNumberSmallerThan(inputValue, comparativeValue);
  }

  private ConditionNumberSmallerThan(String inputValue, String comparativeValue) {
    super(inputValue, comparativeValue);
  }

  @Override
  public ConditionResult compare(Map<String, Object> information) {
    dissolve(information);
    if (!inputValue().matches("-?\\d+(\\.\\d+)?") || !comparativeValue().matches("-?\\d+(\\.\\d+)?")) {
      return ConditionResult.failure("condition.not.a.number");
    }
    return ConditionResult.success(Integer.parseInt(inputValue()) <
      Integer.parseInt(comparativeValue()));
  }
}
