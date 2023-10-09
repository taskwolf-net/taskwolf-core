package de.flexpedite.core.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class DatabaseColumn {
  public static DatabaseColumn create(String name, DatabaseDataType dataType) {
    return create(name, dataType, Type.REGULAR);
  }

  public enum Type {
    PRIMARY_KEY,
    REGULAR;

    public boolean isPrimaryKey() {
      return this == PRIMARY_KEY;
    }

    public boolean isRegular() {
      return this == REGULAR;
    }
  }

  private final String name;
  private final DatabaseDataType dataType;
  private final Type type;

  public String databaseEntry() {
    StringBuilder entry = new StringBuilder();
    entry.append(name);
    entry.append(" ");
    entry.append(dataType);
    if (type.isPrimaryKey()) {
      entry.append(" PRIMARY KEY");
    }
    return entry.toString();
  }
}
