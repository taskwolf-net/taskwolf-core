package de.flexpedite.core.database;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DatabaseTable {
  private final DatabaseConnection connection;
  private final DatabaseKeyspace keyspace;
  private final String name;
  private final List<DatabaseColumn> columns;

  public void create() {
    create("");
  }

  public void createIfNotExists() {
    create("IF NOT EXISTS ");
  }

  private void create(String addition) {
    var query = new StringBuilder("CREATE TABLE ");
    query.append(addition);
    query.append(fullName());
    query.append(" (");
    query.append(columnCompilation());
    query.append(");");
    connection.session().executeAsync(query.toString());
  }

  private String columnCompilation() {
    var compilation = new StringBuilder();
    for (var i = 0; i < columns.size(); i++) {
      compilation.append(columns.get(i).databaseEntry());
      if (i < columns.size() - 1) {
        compilation.append(", ");
      }
    }
    return compilation.toString();
  }

  public void addColumn(DatabaseColumn column) {
    var query = new StringBuilder("ALTER TABLE ");
    query.append(fullName());
    query.append(" ADD ");
    query.append(column.name());
    query.append(" ");
    query.append(column.dataType());
    query.append(";");
    connection.session().executeAsync(query.toString());
  }

  public void dropColumn(String columnName) {
    var query = new StringBuilder("ALTER TABLE ");
    query.append(fullName());
    query.append(" DROP ");
    query.append(columnName);
    query.append(";");
    connection.session().executeAsync(query.toString());
  }

  protected void insert(DatabaseRow row) {
    var query = new StringBuilder("INSERT INTO ");
    query.append(fullName());
    query.append(" (");
    query.append(columnNameCompilation());
    query.append(") VALUES (");
    query.append(row.valuesCompilation());
    query.append(");");
    connection.session().executeAsync(query.toString());
  }

  protected void update(DatabaseCell primaryKeyCell, DatabaseRow row) {
    update(primaryKeyCondition(primaryKeyCell), row);
  }

  protected void update(String condition, DatabaseRow row) {
    var query = new StringBuilder("UPDATE ");
    var primaryKeyIndex = columns.indexOf(findPrimaryKeyColumn());
    query.append(fullName());
    query.append(" SET ");
    for (var i = 0; i < columns.size(); i++) {
      if (i == primaryKeyIndex) {
        continue;
      }
      query.append(columns.get(i).name());
      query.append(" = ");
      query.append(row.findCell(i).databaseValue());
      if (i < columns.size() - 1) {
        query.append(", ");
      }
    }
    query.append(" WHERE ");
    query.append(condition);
    query.append(";");
    connection.session().executeAsync(query.toString());
  }

  protected CompletableFuture<Boolean> exists(DatabaseCell primaryKeyCell) {
    return exists(primaryKeyCondition(primaryKeyCell));
  }

  protected CompletableFuture<Boolean> exists(String condition) {
    var query = new StringBuilder("SELECT ");
    query.append(columnNameCompilation());
    query.append(" FROM ");
    query.append(fullName());
    query.append(" WHERE ");
    query.append(condition);
    query.append(";");
    var result = connection.session().executeAsync(query.toString());
    var futureResponse = new CompletableFuture<Boolean>();
    result.thenAccept(resultSet -> futureResponse.complete(resultSet.remaining() > 0));
    return futureResponse;
  }

  protected CompletableFuture<DatabaseRow> selectRow(DatabaseCell primaryKeyCell) {
    return selectRow(primaryKeyCondition(primaryKeyCell));
  }

  protected CompletableFuture<DatabaseRow> selectRow(String condition) {
    var futureResponse = new CompletableFuture<DatabaseRow>();
    selectRows(condition).thenAccept(rows -> futureResponse.complete(rows.get(0)));
    return futureResponse;
  }

  protected CompletableFuture<List<DatabaseRow>> selectRows(String condition) {
    var query = new StringBuilder("SELECT ");
    query.append(columnNameCompilation());
    query.append(" FROM ");
    query.append(fullName());
    query.append(" WHERE ");
    query.append(condition);
    query.append(";");
    var result = connection.session().executeAsync(query.toString());
    var futureResponse = new CompletableFuture<List<DatabaseRow>>();
    result.thenAccept(resultSet -> futureResponse.complete(
      DatabaseRow.multiple(resultSet.currentPage(), columns.size())));
    return futureResponse;
  }

  private String columnNameCompilation() {
    var compilation = new StringBuilder();
    for (var i = 0; i < columns.size(); i++) {
      compilation.append(columns.get(i).name());
      if (i < columns.size() - 1) {
        compilation.append(", ");
      }
    }
    return compilation.toString();
  }

  protected void delete(DatabaseCell primaryKeyCell) {
    delete(primaryKeyCondition(primaryKeyCell));
  }

  protected void delete(String condition) {
    var query = new StringBuilder("DELETE FROM ");
    query.append(fullName());
    query.append(" WHERE ");
    query.append(condition);
    query.append(";");
    connection.session().executeAsync(query.toString());
  }

  public void drop() {
    drop("");
  }

  public void dropIfExists() {
    drop("IF EXISTS ");
  }

  private void drop(String addition) {
    var query = new StringBuilder("DROP TABLE ");
    query.append(addition);
    query.append(fullName());
    query.append(";");
    connection.session().executeAsync(query.toString());
  }

  private String primaryKeyCondition(DatabaseCell primaryKeyCell) {
    var condition = new StringBuilder(findPrimaryKeyColumn().name());
    condition.append(" = ");
    condition.append(primaryKeyCell.databaseValue());
    return condition.toString();
  }

  private DatabaseColumn findPrimaryKeyColumn() {
    for (var column : columns) {
      if (column.type() == DatabaseColumn.Type.PRIMARY_KEY) {
        return column;
      }
    }
    return null;
  }

  public String fullName() {
    return keyspace.name() + "." + name;
  }
}
