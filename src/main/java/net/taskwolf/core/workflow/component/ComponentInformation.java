package net.taskwolf.core.workflow.component;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ComponentInformation {
  private final String name;
  private final String description;
  private final String identifier;
  private final List<ComponentVariable> inputVariables;
  private final List<ComponentVariable> outputVariables;
}
