package net.taskwolf.core.database;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DatabaseColumn {
  public static DatabaseColumn create(String name, DatabaseDataType dataType) {
    return create(name, dataType, Type.REGULAR);
  }

  public static DatabaseColumn create(
    String name, DatabaseDataType dataType, Type type
  ) {
    return new DatabaseColumn(name, dataType, type);
  }

  public enum Type {
    PRIMARY_KEY,
    PARTITION_KEY,
    CLUSTERING_KEY,
    REGULAR;

    public boolean isPrimaryKey() {
      return this == PRIMARY_KEY;
    }

    public boolean isPartitionKey() {
      return this == PARTITION_KEY;
    }

    public boolean isClusteringKey() {
      return this == CLUSTERING_KEY;
    }

    public boolean isRegular() {
      return this == REGULAR;
    }
  }

  private final String name;
  private final DatabaseDataType dataType;
  private final Type type;

  /**
   * Is used by the {@link DatabaseTable} to e.g. initialize the column / table
   * @return The value of the column that can be interpreted by cassandra
   */
  public String databaseEntry() {
    var entry = new StringBuilder();
    entry.append(name);
    entry.append(" ");
    entry.append(dataType);
    return entry.toString();
  }
}
