package net.taskwolf.core.condition;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public final class ConditionInformationBuilder {
  private String name = "Unknown";
  private ConditionDataType dataType;
  private String identifier;

  public ConditionInformationBuilder withName(String name) {
    this.name = name;
    return this;
  }

  public ConditionInformationBuilder withDataType(ConditionDataType dataType) {
    this.dataType = dataType;
    return this;
  }

  public ConditionInformationBuilder withIdentifier(String identifier) {
    this.identifier = identifier;
    return this;
  }

  public ConditionInformation build() {
    return ConditionInformation.create(name, dataType, identifier);
  }
}

