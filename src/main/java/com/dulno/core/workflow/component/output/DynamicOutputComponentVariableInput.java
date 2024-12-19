package com.dulno.core.workflow.component.output;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.json.JSONObject;

import java.util.List;

@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class DynamicOutputComponentVariableInput {
  @Getter
  private final JSONObject currentContent;
  private final List<JSONObject> previousComponents;

  public List<JSONObject> previousActions() {
    return List.copyOf(previousComponents);
  }
}
