package net.taskwolf.core.database;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.paging.OffsetPager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DatabaseTable {
  @Getter(AccessLevel.PROTECTED)
  private final DatabaseConnection connection;
  @Getter(AccessLevel.PROTECTED)
  private final DatabaseKeyspace keyspace;
  @Getter
  private final String name;
  @Getter
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

  public void createIndex(String column) {
    createIndex(column, "");
  }

  public void createIndexIfNotExists(String column) {
    createIndex(column, "IF NOT EXISTS");
  }

  private void createIndex(String column, String addition) {
    var query = new StringBuilder("CREATE INDEX ");
    query.append(addition);
    query.append(" ON ");
    query.append(fullName());
    query.append(" (");
    query.append(column);
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

  public void renameColumn(String oldColumnName, String newColumnName) {
    var query = new StringBuilder("ALTER TABLE ");
    query.append(fullName());
    query.append(" RENAME ");
    query.append(oldColumnName);
    query.append(" TO ");
    query.append(newColumnName);
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

  protected CompletableFuture<Void> insert(DatabaseRow row) {
    var query = new StringBuilder("INSERT INTO ");
    query.append(fullName());
    query.append(" (");
    query.append(columnNameCompilation());
    query.append(") VALUES (");
    query.append(row.valuesCompilation());
    query.append(");");
    var result = connection.session().executeAsync(query.toString());
    var futureResponse = new CompletableFuture<Void>();
    result.thenAccept(resultSet -> futureResponse.complete(null));
    return futureResponse;
  }

  protected CompletableFuture<Void> update(
    DatabaseCell primaryKeyCell, DatabaseRow row
  ) {
    return update(primaryKeyCondition(primaryKeyCell), row);
  }

  protected CompletableFuture<Void> update(String condition, DatabaseRow row) {
    var query = new StringBuilder("UPDATE ");
    query.append(fullName());
    query.append(" SET ");
    query.append(buildUpdateKeyValuePairs(row));
    query.append(" WHERE ");
    query.append(condition);
    query.append(";");
    var result = connection.session().executeAsync(query.toString());
    var futureResponse = new CompletableFuture<Void>();
    result.thenAccept(resultSet -> futureResponse.complete(null));
    return futureResponse;
  }

  private String buildUpdateKeyValuePairs(DatabaseRow row) {
    var pairs = new StringBuilder();
    var primaryKeyIndex = columns.indexOf(findPrimaryKeyColumn());
    for (var i = 0; i < columns.size(); i++) {
      if (i == primaryKeyIndex) {
        continue;
      }
      pairs.append(columns.get(i).name());
      pairs.append(" = ");
      pairs.append(row.findCell(i).databaseValue());
      if (i < columns.size() - 1) {
        pairs.append(", ");
      }
    }
    return pairs.toString();
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

  public CompletableFuture<Long> count() {
    return count("");
  }

  protected CompletableFuture<Long> count(String addition) {
    var query = new StringBuilder("SELECT COUNT(*) FROM ");
    query.append(fullName());
    query.append(" ");
    query.append(addition);
    query.append(";");
    var result = connection.session().executeAsync(query.toString());
    var futureResponse = new CompletableFuture<Long>();
    result.thenAccept(resultSet -> futureResponse.complete(
      resultSet.one().get(0, Long.class)));
    return futureResponse;
  }

  public CompletableFuture<Long> averageRowSize() {
    return averageRowSize(10);
  }

  protected CompletableFuture<Long> averageRowSize(int samples) {
    var query = new StringBuilder("SELECT * FROM ");
    query.append(fullName());
    query.append(" LIMIT ");
    query.append(samples);
    query.append(";");
    var result = connection.session().executeAsync(query.toString());
    var futureResponse = new CompletableFuture<Long>();
    result.thenAccept(resultSet -> futureResponse.complete(
      calculateAverageRowSize(resultSet)));
    return futureResponse;
  }

  private long calculateAverageRowSize(AsyncResultSet resultSet) {
    var rows = resultSet.currentPage();
    var rowsNumber = 0;
    var sum = 0D;
    for (var row : rows) {
      rowsNumber++;
      var columnsNumber = row.size();
      for (var i = 0; i < columnsNumber; i++) {
        sum += row.getBytesUnsafe(i).remaining();
      }
    }
    return rowsNumber > 0 ? Math.round(sum / rowsNumber) : 0;
  }

  protected CompletableFuture<List<DatabaseRow>> selectAllRows() {
    return selectRowsWithAddition("");
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
    return selectRowsWithAddition(" WHERE " + condition);
  }

  private CompletableFuture<List<DatabaseRow>> selectRowsWithAddition(String addition) {
    var query = new StringBuilder("SELECT ");
    query.append(columnNameCompilation());
    query.append(" FROM ");
    query.append(fullName());
    query.append(addition);
    query.append(";");
    var result = connection.session().executeAsync(query.toString());
    var futureResponse = new CompletableFuture<List<DatabaseRow>>();
    result.thenAccept(resultSet -> futureResponse.complete(
      DatabaseRow.multiple(resultSet.currentPage(), columns.size())));
    return futureResponse;
  }

  protected CompletableFuture<List<DatabaseRow>> selectPagesRows(
    int pageSize, int pageNumber
  ) {
    var query = new StringBuilder("SELECT ");
    query.append(columnNameCompilation());
    query.append(" FROM ");
    query.append(fullName());
    query.append(";");
    var statement = SimpleStatement.builder(query.toString())
      .setPageSize(pageSize).build();
    var result = connection.session().executeAsync(statement);
    var futureResponse = new CompletableFuture<List<DatabaseRow>>();
    result.thenAccept(resultSet -> findCorrectPage(pageSize, pageNumber,
      resultSet).thenAccept(futureResponse::complete));
    return futureResponse;
  }

  private CompletionStage<List<DatabaseRow>> findCorrectPage(
    int pageSize, int pageNumber, AsyncResultSet resultSet
  ) {
    var pager = new OffsetPager(pageSize);
    return pager.getPage(resultSet, pageNumber).thenApply(page ->
      DatabaseRow.multiple(page.getElements(), columns.size()));
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

  protected CompletableFuture<Void> delete(DatabaseCell primaryKeyCell) {
    return delete(primaryKeyCondition(primaryKeyCell));
  }

  protected CompletableFuture<Void> delete(String condition) {
    var query = new StringBuilder("DELETE FROM ");
    query.append(fullName());
    query.append(" WHERE ");
    query.append(condition);
    query.append(";");
    var result = connection.session().executeAsync(query.toString());
    var futureResponse = new CompletableFuture<Void>();
    result.thenAccept(resultSet -> futureResponse.complete(null));
    return futureResponse;
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

  protected void fillColumns(List<DatabaseColumn> newColumns) {
    columns.clear();
    columns.addAll(newColumns);
  }

  public String fullName() {
    return keyspace.name() + "." + name;
  }
}
