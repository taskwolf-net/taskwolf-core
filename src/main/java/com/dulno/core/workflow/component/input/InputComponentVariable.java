package com.dulno.core.workflow.component.input;

import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.workflow.component.ComponentVariable;

@Getter
@Accessors(fluent = true)
public class InputComponentVariable extends ComponentVariable {
  public static InputComponentVariable createSelect(
    String displayName, String identifier, String description,
    InputComponentSelect select
  ) {
    return new InputComponentVariable(displayName, identifier, description,
      "", InputComponentDataType.SELECT, InputComponentType.REQUIRED, select);
  }

  public static InputComponentVariable createRequired(
    String displayName, String identifier, String description,
    InputComponentDataType dataType
  ) {
    return new InputComponentVariable(displayName, identifier, description,
      displayName, dataType, InputComponentType.REQUIRED, null);
  }

  public static InputComponentVariable createRequired(
    String displayName, String identifier, String description, String placeholder,
    InputComponentDataType dataType
  ) {
    return new InputComponentVariable(displayName, identifier, description,
      placeholder, dataType, InputComponentType.REQUIRED, null);
  }

  public static InputComponentVariable createOptional(
    String displayName, String identifier, String description,
    InputComponentDataType dataType
  ) {
    return new InputComponentVariable(displayName, identifier, description,
      displayName, dataType, InputComponentType.OPTIONAL, null);
  }

  public static InputComponentVariable createOptional(
    String displayName, String identifier, String description, String placeholder,
    InputComponentDataType dataType
  ) {
    return new InputComponentVariable(displayName, identifier, description,
      placeholder, dataType, InputComponentType.OPTIONAL, null);
  }

  public static InputComponentVariable create(
    String displayName, String identifier, String description, String placeholder,
    InputComponentDataType dataType, InputComponentType type
  ) {
    return new InputComponentVariable(displayName, identifier, description,
      placeholder, dataType, type, null);
  }

  private final String description;
  private final String placeholder;
  private final InputComponentDataType dataType;
  private final InputComponentType type;
  private final InputComponentSelect select;

  protected InputComponentVariable(
    String displayName, String identifier, String description, String placeholder,
    InputComponentDataType dataType, InputComponentType type, InputComponentSelect select
  ) {
    super(displayName, identifier);
    this.description = description;
    this.placeholder = placeholder;
    this.dataType = dataType;
    this.type = type;
    this.select = select;
  }
}
