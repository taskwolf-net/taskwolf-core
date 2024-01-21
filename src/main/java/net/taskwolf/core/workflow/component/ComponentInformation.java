package net.taskwolf.core.workflow.component;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.workflow.component.input.InputComponentVariable;
import net.taskwolf.core.workflow.component.output.OutputComponentVariable;

import java.util.List;

@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ComponentInformation {
  @Getter
  private final String name;
  @Getter
  private final String description;
  @Getter
  private final String identifier;
  private final List<InputComponentVariable> inputVariables;
  private final List<OutputComponentVariable> outputVariables;

  public List<InputComponentVariable> inputVariables() {
    return List.copyOf(inputVariables);
  }

  public List<ComponentVariable> outputVariables() {
    return List.copyOf(outputVariables);
  }
}
