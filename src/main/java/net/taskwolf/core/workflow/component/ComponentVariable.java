package net.taskwolf.core.workflow.component;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ComponentVariable {
  public static ComponentVariable createSelect(
    String displayName, String identifier, ComponentSelect select
  ) {
    return new ComponentVariable(displayName, identifier, ComponentType.REQUIRED,
      ComponentDataType.SELECT, select);
  }

  public static ComponentVariable create(
    String displayName, String identifier, ComponentType type,
    ComponentDataType dataType
  ) {
    return new ComponentVariable(displayName, identifier, type, dataType, null);
  }

  private final String displayName;
  private final String identifier;
  private final ComponentType type;
  private final ComponentDataType dataType;
  private final ComponentSelect select;
}
