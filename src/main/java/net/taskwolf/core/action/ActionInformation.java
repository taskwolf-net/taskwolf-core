package net.taskwolf.core.action;

import net.taskwolf.core.workflow.component.ComponentInformation;
import net.taskwolf.core.workflow.component.ComponentNovelty;
import net.taskwolf.core.workflow.component.input.InputComponentVariable;
import net.taskwolf.core.workflow.component.output.OutputComponentVariable;

import java.util.List;

public final class ActionInformation extends ComponentInformation {
  public static ActionInformationBuilder builder() {
    return ActionInformationBuilder.create();
  }

  public static ActionInformation create(
    String name, String description, ComponentNovelty novelty,
    List<InputComponentVariable> inputVariables,
    List<OutputComponentVariable> outputVariables
  ) {
    return new ActionInformation(name, description, novelty,
      inputVariables, outputVariables);
  }

  private ActionInformation(
    String name, String description, ComponentNovelty novelty,
    List<InputComponentVariable> inputVariables,
    List<OutputComponentVariable> outputVariables
  ) {
    super(name, description, novelty, inputVariables,
      outputVariables);
  }
}

