package net.taskwolf.core.module;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class ModuleInformation {
  public enum Type {
    PUBLIC,
    HIDDEN;

    public boolean isPublic() {
      return this == PUBLIC;
    }

    public boolean isHidden() {
      return this == HIDDEN;
    }
  }

  private final String name;
  private final String description;
  private final String logo;
  private final Type type;
}
