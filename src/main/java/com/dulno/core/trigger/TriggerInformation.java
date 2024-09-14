package com.dulno.core.trigger;

import com.dulno.core.workflow.component.ComponentInformation;
import com.dulno.core.workflow.component.ComponentNovelty;
import com.dulno.core.workflow.component.input.InputComponentVariable;
import com.dulno.core.workflow.component.output.OutputComponentVariable;

import java.util.List;

public final class TriggerInformation extends ComponentInformation {
  public static TriggerInformationBuilder builder() {
    return TriggerInformationBuilder.create();
  }

  public static TriggerInformation create(
    String name, String description, ComponentNovelty novelty,
    List<InputComponentVariable> inputVariables,
    List<OutputComponentVariable> outputVariables
  ) {
    return new TriggerInformation(name, description, novelty,
      inputVariables, outputVariables);
  }

  private TriggerInformation(
    String name, String description, ComponentNovelty novelty,
    List<InputComponentVariable> inputVariables,
    List<OutputComponentVariable> outputVariables
  ) {
    super(name, description, novelty, inputVariables, outputVariables);
  }
}
