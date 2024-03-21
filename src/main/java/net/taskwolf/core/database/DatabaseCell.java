package net.taskwolf.core.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
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
    if (value instanceof List<?> && !((List<?>) value).isEmpty() &&
      ((List<?>) value).get(0) instanceof String
    ) {
      return ((List<String>) value).stream()
        .map(value -> "'" + value + "'").toList().toString();
    }
    if (value == null) {
      return null;
    }
    return value.toString();
  }

  public int integerValue() {
    if (!(value instanceof Integer)) {
      return -1;
    }
    return (int) value;
  }

  public long longValue() {
    if (!(value instanceof Long)) {
      return -1;
    }
    return (long) value;
  }

  public boolean booleanValue() {
    if (!(value instanceof Boolean)) {
      return false;
    }
    return (boolean) value;
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

  public <T> List<T> listValue() {
    if (!(value instanceof List<?>)) {
      return null;
    }
    return (List<T>) value;
  }

  public Object rawValue() {
    return value;
  }
}
