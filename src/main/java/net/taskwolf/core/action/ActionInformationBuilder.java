package net.taskwolf.core.action;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.workflow.component.ComponentVariable;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public final class ActionInformationBuilder {
  private String name = "Unknown";
  private String description = "";
  private final List<ComponentVariable> inputVariables = Lists.newArrayList();
  private final List<ComponentVariable> outputVariables = Lists.newArrayList();

  public ActionInformationBuilder withName(String name) {
    this.name = name;
    return this;
  }

  public ActionInformationBuilder withDescription(String description) {
    this.description = description;
    return this;
  }

  public ActionInformationBuilder withInputVariable(ComponentVariable variable) {
    inputVariables.add(variable);
    return this;
  }

  public ActionInformationBuilder withOutputVariable(ComponentVariable variable) {
    outputVariables.add(variable);
    return this;
  }

  public ActionInformation build() {
    return ActionInformation.create(name, description, inputVariables,
      outputVariables);
  }
}
