package net.taskwolf.core.workflow.component;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.stream.Stream;

@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ComponentInformation {
  @Getter
  private final String name;
  @Getter
  private final String description;
  @Getter
  private final String identifier;
  private final List<ComponentVariable> inputVariables;
  private final List<ComponentVariable> outputVariables;

  public List<ComponentVariable> inputVariables() {
    return List.copyOf(inputVariables);
  }

  public List<ComponentVariable> outputVariables() {
    return Stream.concat(inputVariables.stream(),
      outputVariables.stream()).toList();
  }
}
