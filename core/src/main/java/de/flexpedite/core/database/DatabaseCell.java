package de.flexpedite.core.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.UUID;

@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class DatabaseCell {
  @Getter
  private final Object value;

  public String databaseValue() {
    if (value instanceof String) {
      return "'" + value + "'";
    }
    return value.toString();
  }

  public int integerValue() {
    if (!(value instanceof Integer)) {
      return -1;
    }
    return (int) value;
  }

  public String stringValue() {
    if (!(value instanceof String)) {
      return null;
    }
    return (String) value;
  }

  public UUID uuidValue() {
    if (!(value instanceof UUID)) {
      return null;
    }
    return (UUID) value;
  }
}
