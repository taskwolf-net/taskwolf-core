package net.taskwolf.core.workflow.component;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class ComponentVariable {
  private final String displayName;
  private final String identifier;
  private final ComponentType type;
}
