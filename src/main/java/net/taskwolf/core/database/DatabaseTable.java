package net.taskwolf.core.database;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.PagingState;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.paging.OffsetPager;
import com.google.common.collect.Lists;
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

  /**
   * Creates the database table even if it already exists
   */
  public void create() {
    create("");
  }

  /**
   * Creates the database table only if it does not already exist
   */
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

  /**
   * Creates an index for a column of the database table even if it already exists
   * @param column The column for which the index is to be created
   */
  public void createIndex(String column) {
    createIndex(column, "");
  }

  /**
   * Creates an index for a column of the database table if it does not already exist
   * @param column The column for which the index is to be created
   */
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
      compilation.append(", ");
    }
    compilation.append("PRIMARY KEY (");
    compilation.append(columnNameCompilation(columns.stream()
      .filter(column -> column.type().isPartitionKey()).toList(), "(", "),"));
    compilation.append(columnNameCompilation(columns.stream()
      .filter(column -> column.type().isClusteringKey()).toList(), "", ""));
    compilation.append(columnNameCompilation(columns.stream()
      .filter(column -> column.type().isPrimaryKey()).toList(), "", ""));
    compilation.append(")");
    return compilation.toString();
  }

  private String columnNameCompilation(
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
    var result = connection.session().executeAsync(query.toString());
    var futureResponse = new CompletableFuture<Void>();
    result.thenAccept(resultSet -> futureResponse.complete(null));
    return futureResponse;
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
    connection.session().executeAsync(query.toString());
    var result = connection.session().executeAsync(query.toString());
    var futureResponse = new CompletableFuture<Void>();
    result.thenAccept(resultSet -> futureResponse.complete(null));
    return futureResponse;
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
    connection.session().executeAsync(query.toString());
    var result = connection.session().executeAsync(query.toString());
    var futureResponse = new CompletableFuture<Void>();
    result.thenAccept(resultSet -> futureResponse.complete(null));
    return futureResponse;
  }

  /**
   * Inserts a new database row into the database table
   * @param row The database row that is to be inserted
   * @return A future that is completed when the insertion is completed
   */
  protected CompletableFuture<Void> insert(DatabaseRow row) {
    return insert(row, "");
  }

  /**
   * Inserts a new database row into the database table
   * @param row The database row that is to be inserted
   * @param addition An addition insertion argument (for example for ttl)
   * @return A future that is completed when the insertion is completed
   */
  protected CompletableFuture<Void> insert(DatabaseRow row, String addition) {
    var query = new StringBuilder("INSERT INTO ");
    query.append(fullName());
    query.append(" (");
    query.append(columnNameCompilation());
    query.append(") VALUES (");
    query.append(row.valuesCompilation());
    query.append(") ");
    query.append(addition);
    query.append(";");
    var result = connection.session().executeAsync(query.toString());
    var futureResponse = new CompletableFuture<Void>();
    result.thenAccept(resultSet -> futureResponse.complete(null));
    return futureResponse;
  }

  /**
   * Updates a row inside the database table
   * @param primaryKeyCell The primary key cell of the row
   * @param row The updated row (with all the columns)
   * @return A future that is completed when the update is completed
   */
  protected CompletableFuture<Void> update(
    DatabaseCell primaryKeyCell, DatabaseRow row
  ) {
    return update(primaryKeyCondition(primaryKeyCell), row);
  }

  /**
   * Updates a row inside the database table
   * @param condition The condition with which the row can be found
   * @param row The updated row (with all the columns)
   * @return A future that is completed when the update is completed
   */
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
    for (var i = 0; i < columns.size(); i++) {
      var column = columns.get(i);
      if (!column.type().isRegular()) {
        continue;
      }
      pairs.append(column.name());
      pairs.append(" = ");
      pairs.append(row.findCell(i).databaseValue());
      if (i < columns.size() - 1) {
        pairs.append(", ");
      }
    }
    return pairs.toString();
  }

  /**
   * Is used to check whether a row inside the database table exists
   * @param primaryKeyCell The primary key cell of the row
   * @return A future that contains the existence boolean
   */
  protected CompletableFuture<Boolean> exists(DatabaseCell primaryKeyCell) {
    if (primaryKeyCell.rawValue() == null) {
      return CompletableFuture.completedFuture(false);
    }
    return exists(primaryKeyCondition(primaryKeyCell));
  }

  /**
   * Is used to check whether a row inside the database table exists
   * @param condition The condition with which the row can be found
   * @return A future that contains the existence boolean
   */
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

  /**
   * Is used to find the number of rows inside a database table
   * @return The number of rows
   */
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

  /**
   * Finds all available rows inside the database table
   * @return List of all possible rows
   */
  protected CompletableFuture<List<DatabaseRow>> selectAllRows() {
    return selectRowsWithAddition("");
  }

  /**
   * Is used to find a single row
   * @param primaryKeyCell The primary key cell of the row
   * @return A future that contains the database row
   */
  protected CompletableFuture<DatabaseRow> selectRow(DatabaseCell primaryKeyCell) {
    return selectRow(primaryKeyCondition(primaryKeyCell));
  }

  /**
   * Is used to find a single row
   * @param condition The condition with which the row can be found
   * @return A future that contains the database row
   */
  protected CompletableFuture<DatabaseRow> selectRow(String condition) {
    var futureResponse = new CompletableFuture<DatabaseRow>();
    selectRows(condition).thenAccept(rows -> futureResponse.complete(rows.get(0)));
    return futureResponse;
  }

  /**
   * Is used to find a multiple rows
   * @param condition The condition with which the rows can be found
   * @return A future that contains the database rows
   */
  protected CompletableFuture<List<DatabaseRow>> selectRows(String condition) {
    return selectRowsWithAddition(" WHERE " + condition);
  }

  protected CompletableFuture<List<DatabaseRow>> selectRowsWithAddition(String addition) {
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

  /**
   * Finds multiple database rows paged
   * @param pageSize The size of each individual page
   * @param pageNumber The current page number
   * @param condition The condition with which the rows can be found
   * @return A future that contains the page
   */
  protected CompletableFuture<DatabasePage<DatabaseRow>> selectPage(
    int pageSize, int pageNumber, String condition
  ) {
    return selectPageWithAddition(pageSize, pageNumber, " WHERE " + condition);
  }

  protected CompletableFuture<DatabasePage<DatabaseRow>> selectPageWithAddition(
    int pageSize, int pageNumber, String addition
  ) {
    var query = new StringBuilder("SELECT ");
    query.append(columnNameCompilation());
    query.append(" FROM ");
    query.append(fullName());
    query.append(addition);
    query.append(";");
    var statement = SimpleStatement.builder(query.toString())
      .setPageSize(pageSize).build();
    var result = connection.session().executeAsync(statement);
    var futureResponse = new CompletableFuture<DatabasePage<DatabaseRow>>();
    result.thenAccept(resultSet -> findCorrectPage(pageSize, pageNumber,
      resultSet).thenAccept(futureResponse::complete));
    return futureResponse;
  }

  private CompletionStage<DatabasePage<DatabaseRow>> findCorrectPage(
    int pageSize, int pageNumber, AsyncResultSet resultSet
  ) {
    /*var pager = new OffsetPager(pageSize);
    return pager.getPage(resultSet, pageNumber).thenApply(page ->
      DatabaseRow.multiple(page.getElements(), columns.size()));*/
    //TODO: IMPLEMENT OWN OffsetPager TO GET PAGE STATE OF TARGET PAGE FOR THE
    // DatabasePage (FUNCTION RETURN)
    return null;
  }

  /**
   * Used to shift an existing paging state (next or previous page)
   * @param partitionKeyCell The partition key value that specifies the
   *                         basic set of elements to be paged
   * @param clusteringKeyColumn The clustering key column used for sorting
   * @param order The direction in which sorting should take place
   * @param pageSize The page size that is used for the paging process
   * @param pageState The current page state
   * @param direction The direction in which you want to shift
   * @return A future that contains the page
   */
  protected CompletableFuture<DatabasePage<DatabaseRow>> shiftPage(
    DatabaseCell partitionKeyCell, String clusteringKeyColumn, DatabaseOrder order,
    int pageSize, String pageState, DatabaseDirection direction
  ) {
    var query = new StringBuilder("SELECT ");
    query.append(columnNameCompilation());
    query.append(" FROM ");
    query.append(fullName());
    query.append(" WHERE ");
    query.append(partitionKeyCondition(partitionKeyCell));
    query.append(" ORDER BY ");
    query.append(clusteringKeyColumn);
    query.append(" ");
    query.append(direction.isForward() ? order.value() : order.reverse().value());
    query.append(";");
    var rawState = PagingState.fromString(pageState).getRawPagingState();
    var statement = SimpleStatement.builder(query.toString())
      .setPageSize(pageSize).setPagingState(rawState).build();
    var result = connection.session().executeAsync(statement);
    var futureResponse = new CompletableFuture<DatabasePage<DatabaseRow>>();
    result.thenCompose(this::finishPageShifting)
      .thenAccept(futureResponse::complete);
    return futureResponse;
  }

  private CompletionStage<DatabasePage<DatabaseRow>> finishPageShifting(
    AsyncResultSet resultSet
  ) {
    if (!resultSet.hasMorePages()) {
      return CompletableFuture.completedFuture(null);
    }
    return resultSet.fetchNextPage().thenApply(this::createDatabasePage);
  }

  private DatabasePage<DatabaseRow> createDatabasePage(AsyncResultSet resultSet) {
    return DatabasePage.create(
      DatabaseRow.multiple(Lists.newArrayList(resultSet.currentPage()),
        columns.size()),
      resultSet.getExecutionInfo().getSafePagingState().toString());
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

  /**
   * Deletes a database row from the database table
   * @param primaryKeyCell The primary key cell of the row
   * @return A future that is completed when the deletion is completed
   */
  protected CompletableFuture<Void> delete(DatabaseCell primaryKeyCell) {
    return delete(primaryKeyCondition(primaryKeyCell));
  }

  /**
   * Deletes a database row from the database table
   * @param condition The condition with which the rows can be found
   * @return A future that is completed when the deletion is completed
   */
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

  /**
   * Deletes the database table and all its content
   */
  public void drop() {
    drop("");
  }

  /**
   * Deletes the database table and all its content only if it exists
   */
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
    return columnCondition(columns.stream()
      .filter(column -> column.type().isPrimaryKey())
      .findFirst().get(), primaryKeyCell);
  }

  private String partitionKeyCondition(DatabaseCell partitionKeyCell) {
    return columnCondition(columns.stream()
      .filter(column -> column.type().isPartitionKey())
      .findFirst().get(), partitionKeyCell);
  }

  private String columnCondition(DatabaseColumn column, DatabaseCell cell) {
    return columnCondition(column.name(), cell);
  }

  private String columnCondition(String columnName, DatabaseCell cell) {
    var condition = new StringBuilder(columnName);
    condition.append(" = ");
    condition.append(cell.databaseValue());
    return condition.toString();
  }

  protected void fillColumns(List<DatabaseColumn> newColumns) {
    columns.clear();
    columns.addAll(newColumns);
  }

  /**
   * Build the full name of the database table
   * @return The full name of the database table
   */
  public String fullName() {
    return keyspace.name() + "." + name;
  }
}
