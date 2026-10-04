package net.taskwolf.core.module;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
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

  public enum Novelty {
    NEW,
    OLD;

    public boolean isNew() {
      return this == NEW;
    }

    public boolean isOld() {
      return this == OLD;
    }
  }

  private final String name;
  private final String description;
  private final String logo;
  private final Type type;
  private Novelty novelty = Novelty.OLD;
}
