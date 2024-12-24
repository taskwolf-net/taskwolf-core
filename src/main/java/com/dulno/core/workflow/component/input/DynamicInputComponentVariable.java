package com.dulno.core.workflow.component.input;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class DynamicInputComponentVariable extends InputComponentVariable {
  public static DynamicInputComponentVariable create(
    String requiredPredecessor,
    DynamicInputComponentVariableFunction variableFunction
  ) {
    return new DynamicInputComponentVariable(requiredPredecessor, variableFunction);
  }

  private final String requiredPredecessor;
  private final DynamicInputComponentVariableFunction variableFunction;

  private DynamicInputComponentVariable(
    String requiredPredecessor,
    DynamicInputComponentVariableFunction variableFunction
  ) {
    super("", "", "", "", null, null, null);
    this.requiredPredecessor = requiredPredecessor;
    this.variableFunction = variableFunction;
  }
}