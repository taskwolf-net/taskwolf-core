package net.taskwolf.core.trigger;

import net.taskwolf.core.workflow.component.ComponentInformation;
import net.taskwolf.core.workflow.component.ComponentType;

import java.util.Map;

public final class TriggerInformation extends ComponentInformation {
  public static TriggerInformation create(
    String name, String description, Map<String, ComponentType> inputTypes,
    Map<String, ComponentType> outputTypes
  ) {
    return new TriggerInformation(name, description, inputTypes, outputTypes);
  }

  private TriggerInformation(
    String name, String description, Map<String, ComponentType> inputTypes,
    Map<String, ComponentType> outputTypes
  ) {
    super(name, description, inputTypes, outputTypes);
  }
}
