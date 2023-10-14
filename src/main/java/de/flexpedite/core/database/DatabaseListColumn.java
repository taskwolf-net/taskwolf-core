package de.flexpedite.core.database;

public class DatabaseListColumn extends DatabaseColumn {
  public static DatabaseListColumn create(String name, DatabaseDataType dataType) {
    return create(name, dataType, Type.REGULAR);
  }

  public static DatabaseListColumn create(
    String name, DatabaseDataType dataType, Type type
  ) {
    return new DatabaseListColumn(name, dataType, type);
  }

  private final DatabaseDataType listDataType;

  private DatabaseListColumn(String name, DatabaseDataType listDataType, Type type) {
    super(name, DatabaseDataType.LIST, type);
    this.listDataType = listDataType;
  }

  @Override
  public String databaseEntry() {
    StringBuilder entry = new StringBuilder();
    entry.append(name());
    entry.append(" ");
    entry.append(dataType());
    entry.append("<");
    entry.append(listDataType);
    entry.append(">");
    if (type().isPrimaryKey()) {
      entry.append(" PRIMARY KEY");
    }
    return entry.toString();
  }
}
