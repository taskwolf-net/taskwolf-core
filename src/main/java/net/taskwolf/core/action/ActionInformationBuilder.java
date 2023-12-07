package net.taskwolf.core.action;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.workflow.component.ComponentVariable;
import net.taskwolf.core.workflow.component.input.InputComponentVariable;
import net.taskwolf.core.workflow.component.output.OutputComponentVariable;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public final class ActionInformationBuilder {
  private String name = "Unknown";
  private String description = "";
  private String identifier = "";
  private final List<InputComponentVariable> inputVariables = Lists.newArrayList();
  private final List<OutputComponentVariable> outputVariables = Lists.newArrayList();

  public ActionInformationBuilder withName(String name) {
    this.name = name;
    return this;
  }

  public ActionInformationBuilder withDescription(String description) {
    this.description = description;
    return this;
  }

  public ActionInformationBuilder withIdentifier(String identifier) {
    this.identifier = identifier;
    return this;
  }

  public ActionInformationBuilder withInputVariable(InputComponentVariable variable) {
    inputVariables.add(variable);
    return this;
  }

  public ActionInformationBuilder withOutputVariable(OutputComponentVariable variable) {
    outputVariables.add(variable);
    return this;
  }

  public ActionInformation build() {
    return ActionInformation.create(name, description, identifier,
      inputVariables, outputVariables);
  }
}
