package de.flexpedite.core.database;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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
    StringBuilder query = new StringBuilder("CREATE TABLE ");
    query.append(addition);
    query.append(fullName());
    query.append(" (");
    query.append(columnCompilation());
    query.append(");");
    connection.session().executeAsync(query.toString());
  }

  private String columnCompilation() {
    StringBuilder compilation = new StringBuilder();
    for (int i = 0; i < columns.size(); i++) {
      compilation.append(columns.get(i).databaseEntry());
      if (i < columns.size() - 1) {
        compilation.append(", ");
      }
    }
    return compilation.toString();
  }

  public void addColumn(DatabaseColumn column) {
    StringBuilder query = new StringBuilder("ALTER TABLE ");
    query.append(fullName());
    query.append(" ADD ");
    query.append(column.name());
    query.append(" ");
    query.append(column.dataType());
    query.append(";");
    connection.session().executeAsync(query.toString());
  }

  public void dropColumn(String columnName) {
    StringBuilder query = new StringBuilder("ALTER TABLE ");
    query.append(fullName());
    query.append(" DROP ");
    query.append(columnName);
    query.append(";");
    connection.session().executeAsync(query.toString());
  }

  protected void insert(DatabaseRow row) {
    StringBuilder query = new StringBuilder("INSERT INTO ");
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
    StringBuilder query = new StringBuilder("UPDATE ");
    int primaryKeyIndex = columns.indexOf(findPrimaryKeyColumn());
    query.append(fullName());
    query.append(" SET ");
    for (int i = 0; i < columns.size(); i++) {
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
    query.append(" ");
    query.append(condition);
    query.append(";");
    connection.session().executeAsync(query.toString());
  }

  protected CompletableFuture<Boolean> exists(DatabaseCell primaryKeyCell) {
    return exists(primaryKeyCondition(primaryKeyCell));
  }

  protected CompletableFuture<Boolean> exists(String condition) {
    StringBuilder query = new StringBuilder("SELECT ");
    query.append(columnNameCompilation());
    query.append(" FROM ");
    query.append(fullName());
    query.append(" ");
    query.append(condition);
    query.append(";");
    CompletionStage<AsyncResultSet> result = connection.session()
      .executeAsync(query.toString());
    CompletableFuture<Boolean> futureResponse = new CompletableFuture<>();
    result.thenAccept(resultSet -> futureResponse.complete(resultSet.remaining() > 0));
    return futureResponse;
  }

  protected CompletableFuture<DatabaseRow> selectRow(DatabaseCell primaryKeyCell) {
    return selectRow(primaryKeyCondition(primaryKeyCell));
  }

  protected CompletableFuture<DatabaseRow> selectRow(String condition) {
    CompletableFuture<DatabaseRow> futureResponse = new CompletableFuture<>();
    selectRows(condition).thenAccept(rows -> futureResponse.complete(rows.get(0)));
    return futureResponse;
  }

  protected CompletableFuture<List<DatabaseRow>> selectRows(String condition) {
    StringBuilder query = new StringBuilder("SELECT ");
    query.append(columnNameCompilation());
    query.append(" FROM ");
    query.append(fullName());
    query.append(" ");
    query.append(condition);
    query.append(";");
    CompletionStage<AsyncResultSet> result = connection.session()
      .executeAsync(query.toString());
    CompletableFuture<List<DatabaseRow>> futureResponse = new CompletableFuture<>();
    result.thenAccept(resultSet -> futureResponse.complete(
      DatabaseRow.multiple(resultSet.currentPage(), columns.size())));
    return futureResponse;
  }

  private String columnNameCompilation() {
    StringBuilder compilation = new StringBuilder();
    for (int i = 0; i < columns.size(); i++) {
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
    StringBuilder query = new StringBuilder("DELETE FROM ");
    query.append(fullName());
    query.append(" ");
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
    StringBuilder query = new StringBuilder("DROP TABLE ");
    query.append(addition);
    query.append(fullName());
    query.append(";");
    connection.session().executeAsync(query.toString());
  }

  private String primaryKeyCondition(DatabaseCell primaryKeyCell) {
    StringBuilder condition = new StringBuilder("WHERE ");
    condition.append(findPrimaryKeyColumn().name());
    condition.append(" = ");
    condition.append(primaryKeyCell.databaseValue());
    return condition.toString();
  }

  private DatabaseColumn findPrimaryKeyColumn() {
    for (DatabaseColumn column : columns) {
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
