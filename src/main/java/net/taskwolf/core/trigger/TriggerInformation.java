package net.taskwolf.core.trigger;

import net.taskwolf.core.workflow.component.ComponentInformation;
import net.taskwolf.core.workflow.component.ComponentNovelty;
import net.taskwolf.core.workflow.component.input.InputComponentVariable;
import net.taskwolf.core.workflow.component.output.OutputComponentVariable;

import java.util.List;

public final class TriggerInformation extends ComponentInformation {
  public static TriggerInformationBuilder builder() {
    return TriggerInformationBuilder.create();
  }

  public static TriggerInformation create(
    String name, String description, String identifier, ComponentNovelty novelty,
    List<InputComponentVariable> inputVariables,
    List<OutputComponentVariable> outputVariables
  ) {
    return new TriggerInformation(name, description, identifier, novelty,
      inputVariables, outputVariables);
  }

  private TriggerInformation(
    String name, String description, String identifier, ComponentNovelty novelty,
    List<InputComponentVariable> inputVariables,
    List<OutputComponentVariable> outputVariables
  ) {
    super(name, description, identifier, novelty, inputVariables, outputVariables);
  }
}
