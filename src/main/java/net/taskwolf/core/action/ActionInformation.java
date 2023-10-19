package net.taskwolf.core.action;

import net.taskwolf.core.workflow.component.ComponentInformation;
import net.taskwolf.core.workflow.component.ComponentVariable;

import java.util.List;

public final class ActionInformation extends ComponentInformation {
  public static ActionInformationBuilder builder() {
    return ActionInformationBuilder.create();
  }

  public static ActionInformation create(
    String name, String description, List<ComponentVariable> inputVariables,
    List<ComponentVariable> outputVariables
  ) {
    return new ActionInformation(name, description, inputVariables, outputVariables);
  }

  private ActionInformation(
    String name, String description, List<ComponentVariable> inputVariables,
    List<ComponentVariable> outputVariables
  ) {
    super(name, description, inputVariables, outputVariables);
  }
}

