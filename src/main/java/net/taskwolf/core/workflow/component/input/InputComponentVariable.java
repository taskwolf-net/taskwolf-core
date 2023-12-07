package net.taskwolf.core.workflow.component.input;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.workflow.component.ComponentVariable;

@Getter
@Accessors(fluent = true)
public final class InputComponentVariable extends ComponentVariable {
  public static InputComponentVariable createRequired(
    String displayName, String identifier, InputComponentDataType dataType
  ) {
    return new InputComponentVariable(displayName, identifier, dataType,
      InputComponentType.REQUIRED, null);
  }

  public static InputComponentVariable createOptional(
    String displayName, String identifier, InputComponentDataType dataType
  ) {
    return new InputComponentVariable(displayName, identifier, dataType,
      InputComponentType.OPTIONAL, null);
  }

  public static InputComponentVariable createSelect(
    String displayName, String identifier, InputComponentSelect select
  ) {
    return new InputComponentVariable(displayName, identifier,
      InputComponentDataType.SELECT, InputComponentType.REQUIRED, select);
  }

  public static InputComponentVariable create(
    String displayName, String identifier, InputComponentDataType dataType,
    InputComponentType type
  ) {
    return new InputComponentVariable(displayName, identifier, dataType, type, null);
  }

  private final InputComponentDataType dataType;
  private final InputComponentType type;
  private final InputComponentSelect select;

  private InputComponentVariable(
    String displayName, String identifier, InputComponentDataType dataType,
    InputComponentType type, InputComponentSelect select
  ) {
    super(displayName, identifier);
    this.dataType = dataType;
    this.type = type;
    this.select = select;
  }
}
