package net.taskwolf.core.workflow.placeholder;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor(staticName = "create")
public final class PlaceholderDissolve {
  private final Map<String, Object> context;

  public String dissolve(String value) {
    for (var entry : context.entrySet()) {
      value = value.replace("$" + entry.getKey(), entry.getValue().toString());
    }
    return value;
  }
}
