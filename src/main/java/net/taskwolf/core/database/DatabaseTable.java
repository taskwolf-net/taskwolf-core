package net.taskwolf.core.database;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.database.skeleton.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Accessors(fluent = true)
@RequiredArgsConstructor
public class DatabaseTable implements CreatableDatabaseTable,
  DroppableDatabaseTable, InsertableDatabaseTable, DeletableDatabaseTable,
  ExistableDatabaseTable, SelectableDatabaseTable, UpdatableDatabaseTable,
  CountableDatabaseTable, IndexableDatabaseTable, PageableDatabaseTable,
  ViewableDatabaseTable
{
  private final DatabaseConnection connection;
  private final DatabaseKeyspace keyspace;
  private final String name;
  private final List<DatabaseColumn> columns;

  public DatabaseConnection connection() {
    return connection;
  }

  public DatabaseKeyspace keyspace() {
    return keyspace;
  }

  public String name() {
    return name;
  }

  /**
   * Build the full name of the database table
   * @return The full name of the database table
   */
  public String fullName() {
    return keyspace.name() + "." + name;
  }

  public List<DatabaseColumn> columns() {
    return List.copyOf(columns);
  }

  public String columnNameCompilation() {
    var compilation = new StringBuilder();
    for (var i = 0; i < columns.size(); i++) {
      compilation.append(columns.get(i).name());
      if (i < columns.size() - 1) {
        compilation.append(", ");
      }
    }
    return compilation.toString();
  }

  public String columnNameCompilation(
    List<DatabaseColumn> columns, String prefix, String suffix
  ) {
    if (columns.isEmpty()) {
      return "";
    }
    var compilation = new StringBuilder(prefix);
    for (var i = 0; i < columns.size(); i++) {
      if (i > 0) {
        compilation.append(", ");
      }
      compilation.append(columns.get(i).name());
    }
    compilation.append(suffix);
    return compilation.toString();
  }

  public DatabaseColumn findPrimaryKeyColumn() {
    return columns.stream().filter(column -> column.type().isPrimaryKey())
      .findFirst().get();
  }

  public List<DatabaseColumn> findPrimaryKeyColumns() {
    return columns.stream().filter(column -> column.type().isPrimaryKey())
      .toList();
  }

  public DatabaseColumn findPartitionKeyColumn() {
    return columns.stream().filter(column -> column.type().isPartitionKey())
      .findFirst().get();
  }

  public List<DatabaseColumn> findPartitionKeyColumns() {
    return columns.stream().filter(column -> column.type().isPartitionKey())
      .toList();
  }

  /**
   * Is used to add a new column to the database table
   * @param column The new column
   * @return A future that is completed when the operation is completed
   */
  public CompletableFuture<Void> addColumn(DatabaseColumn column) {
    columns.add(column);
    var query = new StringBuilder("ALTER TABLE ");
    query.append(fullName());
    query.append(" ADD ");
    query.append(column.name());
    query.append(" ");
    query.append(column.dataType());
    query.append(";");
    return connection.execute(query).thenApply(value -> null);
  }

  /**
   * Is used to rename an existing column
   * @param oldColumnName The old name of the column
   * @param newColumnName The new name of the column
   * @return A future that is completed when the operation is completed
   */
  public CompletableFuture<Void> renameColumn(String oldColumnName, String newColumnName) {
    var query = new StringBuilder("ALTER TABLE ");
    query.append(fullName());
    query.append(" RENAME ");
    query.append(oldColumnName);
    query.append(" TO ");
    query.append(newColumnName);
    query.append(";");
    return connection.execute(query).thenApply(value -> null);
  }

  /**
   * Is used to delete a column and all its content
   * @param columnName The name of the column that is to be dropped
   * @return A future that is completed when the operation is completed
   */
  public CompletableFuture<Void> dropColumn(String columnName) {
    var newColumns = columns.stream()
      .filter(column -> !column.name().equalsIgnoreCase(columnName))
      .toList();
    columns.clear();
    columns.addAll(newColumns);
    var query = new StringBuilder("ALTER TABLE ");
    query.append(fullName());
    query.append(" DROP ");
    query.append(columnName);
    query.append(";");
    return connection.execute(query).thenApply(value -> null);
  }

  /**
   * Is used by table system (databases) to configure table asynchronous
   * @param newColumns The columns of the table
   */
  protected void fillColumns(List<DatabaseColumn> newColumns) {
    columns.clear();
    columns.addAll(newColumns);
  }
}
