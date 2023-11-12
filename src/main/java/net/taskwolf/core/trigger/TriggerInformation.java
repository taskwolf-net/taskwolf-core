package net.taskwolf.core.trigger;

import net.taskwolf.core.workflow.component.ComponentInformation;
import net.taskwolf.core.workflow.component.ComponentVariable;

import java.util.List;

public final class TriggerInformation extends ComponentInformation {
  public static TriggerInformationBuilder builder() {
    return TriggerInformationBuilder.create();
  }

  public static TriggerInformation create(
    String name, String description, String identifier,
    List<ComponentVariable> inputVariables,
    List<ComponentVariable> outputVariables
  ) {
    return new TriggerInformation(name, description, identifier,
      inputVariables, outputVariables);
  }

  private TriggerInformation(
    String name, String description, String identifier,
    List<ComponentVariable> inputVariables,
    List<ComponentVariable> outputVariables
  ) {
    super(name, description, identifier, inputVariables, outputVariables);
  }
}
