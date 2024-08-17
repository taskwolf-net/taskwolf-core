package net.taskwolf.core.database;

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

  /**
   * Is used by the {@link DatabaseTable} to e.g. initialize the column / table
   * @return The value of the column that can be interpreted by cassandra
   */
  @Override
  public String databaseEntry() {
    var entry = new StringBuilder();
    entry.append(name());
    entry.append(" ");
    entry.append(dataType());
    entry.append("<");
    entry.append(listDataType);
    entry.append(">");
    return entry.toString();
  }
}
