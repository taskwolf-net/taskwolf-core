package net.taskwolf.core.trigger;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.workflow.component.ComponentNovelty;
import net.taskwolf.core.workflow.component.input.InputComponentVariable;
import net.taskwolf.core.workflow.component.output.OutputComponentVariable;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public final class TriggerInformationBuilder {
  private String name = "Unknown";
  private String description = "";
  private String identifier = "";
  private ComponentNovelty novelty = ComponentNovelty.OLD;
  private final List<InputComponentVariable> inputVariables = Lists.newArrayList();
  private final List<OutputComponentVariable> outputVariables = Lists.newArrayList();

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

  public TriggerInformationBuilder withNovelty(ComponentNovelty novelty) {
    this.novelty = novelty;
    return this;
  }

  public TriggerInformationBuilder withInputVariable(InputComponentVariable variable) {
    inputVariables.add(variable);
    return this;
  }

  public TriggerInformationBuilder withOutputVariable(OutputComponentVariable variable) {
    outputVariables.add(variable);
    return this;
  }

  public TriggerInformation build() {
    return TriggerInformation.create(name, description, identifier, novelty,
      inputVariables, outputVariables);
  }
}
