package net.taskwolf.core.action;

import net.taskwolf.core.workflow.component.ComponentInformation;
import net.taskwolf.core.workflow.component.ComponentType;

import java.util.Map;

public final class ActionInformation extends ComponentInformation {
  public static ActionInformation create(
    String name, String description, Map<String, ComponentType> inputTypes,
    Map<String, ComponentType> outputTypes
  ) {
    return new ActionInformation(name, description, inputTypes, outputTypes);
  }

  private ActionInformation(
    String name, String description, Map<String, ComponentType> inputTypes,
    Map<String, ComponentType> outputTypes
  ) {
    super(name, description, inputTypes, outputTypes);
  }
}

