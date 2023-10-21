package net.taskwolf.core.trigger;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.workflow.component.ComponentVariable;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public final class TriggerInformationBuilder {
  private String name = "Unknown";
  private String description = "";
  private String identifier = "";
  private final List<ComponentVariable> inputVariables = Lists.newArrayList();
  private final List<ComponentVariable> outputVariables = Lists.newArrayList();

  public TriggerInformationBuilder withName(String name) {
    this.name = name;
    return this;
  }

  public TriggerInformationBuilder withDescription(String description) {
    this.description = description;
    return this;
  }

  public TriggerInformationBuilder withIdentifier(String identifier) {
    this.identifier = identifier;
    return this;
  }

  public TriggerInformationBuilder withInputVariable(ComponentVariable variable) {
    inputVariables.add(variable);
    return this;
  }

  public TriggerInformationBuilder withOutputVariable(ComponentVariable variable) {
    outputVariables.add(variable);
    return this;
  }

  public TriggerInformation build() {
    return TriggerInformation.create(name, description, identifier,
      inputVariables, outputVariables);
  }
}
