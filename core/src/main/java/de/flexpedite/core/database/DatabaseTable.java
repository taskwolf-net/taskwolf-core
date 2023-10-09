package de.flexpedite.core.database;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DatabaseTable {
  private final DatabaseConnection connection;
  private final DatabaseKeyspace keyspace;
  private final String name;
  private final List<DatabaseColumn> columns;
  private final Map<Object, DatabaseRow> cache = Maps.newHashMap();

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
    System.out.println(query.toString());
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

  public void insert(DatabaseRow row) {
    StringBuilder query = new StringBuilder("INSERT INTO ");
    query.append(fullName());
    query.append(" (");
    query.append(columnNameCompilation());
    query.append(") VALUES (");
    query.append(row.valuesCompilation());
    query.append(");");
    connection.session().executeAsync(query.toString());
    cache.put(columns.indexOf(findPrimaryKeyColumn()), row);
  }

  public CompletableFuture<DatabaseRow> selectRow(DatabaseCell primaryKeyCell) {
    if (cache.containsKey(primaryKeyCell.value())) {
      return CompletableFuture.completedFuture(cache.get(primaryKeyCell.value()));
    }
    CompletableFuture<DatabaseRow> futureResponse =
      selectRow(primaryKeyCondition(primaryKeyCell));
    futureResponse.thenAccept(row -> cache.put(columns.indexOf(
      findPrimaryKeyColumn()), row));
    return futureResponse;
  }

  public CompletableFuture<DatabaseRow> selectRow(String condition) {
    CompletableFuture<DatabaseRow> futureResponse = new CompletableFuture<>();
    selectRows(condition).thenAccept(rows -> futureResponse.complete(rows.get(0)));
    return futureResponse;
  }

  public CompletableFuture<List<DatabaseRow>> selectRows(String condition) {
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

  public void delete(DatabaseCell primaryKeyCell) {
    delete(primaryKeyCondition(primaryKeyCell));
  }

  public void delete(String condition) {
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
