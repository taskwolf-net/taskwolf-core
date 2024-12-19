package com.dulno.core.workflow.component.output;

import java.util.List;
import java.util.function.Function;

public interface DynamicOutputComponentVariableFunction extends
  Function<DynamicOutputComponentVariableInput, List<OutputComponentVariable>> {
}
