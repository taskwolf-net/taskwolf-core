package com.dulno.core.condition.text;


import com.dulno.core.condition.Condition;
import com.dulno.core.condition.ConditionDataType;
import com.dulno.core.condition.ConditionInformation;
import com.dulno.core.workflow.step.WorkflowStepResult;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class ConditionTextStartsWith extends Condition {
  public static ConditionInformation information() {
    return ConditionInformation.builder()
      .withName("condition.text.starts.with")
      .withDataType(ConditionDataType.TEXT)
      .withIdentifier("condition-text-starts-with").build();
  }

  public static ConditionTextStartsWith create(
    String inputValue, String comparativeValue) {
    return new ConditionTextStartsWith(inputValue, comparativeValue
    );
  }

  private ConditionTextStartsWith(String inputValue, String comparativeValue) {
    super(inputValue, comparativeValue);
  }

  @Override
  public CompletableFuture<WorkflowStepResult> execute(
    Map<String, Object> information
  ) {
    dissolve(information);
    return WorkflowStepResult.futureSuccess(
      inputValue().startsWith(comparativeValue()));
  }
}
