package com.dulno.core.workflow.component.output;

import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.workflow.component.ComponentVariable;

@Getter
@Accessors(fluent = true)
public final class OutputComponentVariable extends ComponentVariable {
  public static OutputComponentVariable create(
    String displayName, String identifier
  ) {
    return new OutputComponentVariable(displayName, identifier);
  }

  private OutputComponentVariable(String displayName, String identifier) {
    super(displayName, identifier);
  }
}
