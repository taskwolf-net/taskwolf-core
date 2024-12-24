package com.dulno.core.workflow.component.input;

import org.json.JSONObject;

import java.util.List;
import java.util.function.Function;

public interface DynamicInputComponentVariableFunction extends
  Function<JSONObject, List<InputComponentVariable>> {
}
