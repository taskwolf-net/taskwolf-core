package net.taskwolf.core.database;

import com.datastax.oss.driver.api.core.cql.*;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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
    query.append(")");
    query.append(clusteringOrder());
    query.append(";");
    connection.execute(query);
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

  private String clusteringOrder() {
    var order = columns.stream().filter(DatabaseColumn::hasOrder).toList();
    if (order.isEmpty()) {
      return "";
    }
    var result = new StringBuilder();
    result.append(" WITH CLUSTERING ORDER BY (");
    for (var i = 0; i < order.size(); i++) {
      if (i > 0) {
        result.append(", ");
      }
      var column = order.get(i);
      result.append(column.name());
      result.append(" ");
      result.append(column.order().value());
    }
    result.append(")");
    return result.toString();
  }

  /**
   * Creates an index for a column of the database table even if it already exists
   * @param column The column for which the index is to be created
   */
  public void createIndex(String column) {
    createIndex(column, "", "");
  }

  /**
   * Creates an index for a column of the database table even if it already exists
   * @param column The column for which the index is to be created
   * @param customType The custom type of the index
   */
  public void createIndex(String column, String customType) {
    createIndex(column, "", customType);
  }

  /**
   * Creates an index for a column of the database table if it does not already exist
   * @param column The column for which the index is to be created
   */
  public void createIndexIfNotExists(String column) {
    createIndex(column, "IF NOT EXISTS", "");
  }

  /**
   * Creates an index for a column of the database table if it does not already exist
   * @param column The column for which the index is to be created
   * @param customType The custom type of the index
   */
  public void createIndexIfNotExists(String column, String customType) {
    createIndex(column, "IF NOT EXISTS", customType);
  }

  private void createIndex(String column, String addition, String customType) {
    var query = new StringBuilder("CREATE");
    if (!customType.isEmpty()) {
      query.append(" CUSTOM");
    }
    query.append(" INDEX ");
    query.append(addition);
    query.append(" ON ");
    query.append(fullName());
    query.append(" (");
    query.append(column);
    query.append(")");
    if (!customType.isEmpty()) {
      query.append(" USING ");
      query.append(customType);
    }
    query.append(";");
    connection.execute(query);
  }

  /**
   * Creates a new materialized view from the table
   * @param name The name of the materialized view
   * @param columns The column settings (primary, partition and clustering columns)
   * @return The materialized view table
   */
  public DatabaseTable createMaterializedView(
    String name, List<DatabaseColumn> columns
  ) {
    return createMaterializedView(name, columns, "");
  }

  /**
   * Creates a new materialized view from the table
   * @param name The name of the materialized view
   * @param clusteringColumnName The name of the column used for clustering
   * @return The materialized view table
   */
  public DatabaseTable createMaterializedViewIfNotExists(
    String name, String clusteringColumnName
  ) {
    var primaryColumns = Lists.newArrayList(columns.stream()
      .filter(column -> column.type().isPartitionKey()).toList());
    primaryColumns.add(columns.stream()
      .filter(column -> column.name().equalsIgnoreCase(clusteringColumnName))
      .map(column -> DatabaseColumn.create(column.name(), column.dataType(),
        DatabaseColumn.Type.CLUSTERING_KEY)).findFirst().get());
    primaryColumns.addAll(columns.stream()
      .filter(column -> column.type().isClusteringKey()).toList());
    return createMaterializedViewIfNotExists(name, primaryColumns);
  }

  /**
   * Creates a new materialized view from the table
   * @param name The name of the materialized view
   * @param columns The column settings (primary, partition and clustering columns)
   * @return The materialized view table
   */
  public DatabaseTable createMaterializedViewIfNotExists(
    String name, List<DatabaseColumn> columns
  ) {
    return createMaterializedView(name, columns, "IF NOT EXISTS ");
  }

  private DatabaseTable createMaterializedView(
    String name, List<DatabaseColumn> columns, String addition
  ) {
    var query = new StringBuilder("CREATE MATERIALIZED VIEW ");
    query.append(addition);
    query.append(fullName() + "_" + name);
    query.append(" AS SELECT * FROM ");
    query.append(fullName());
    query.append(" WHERE ");
    for (var i = 0; i < columns.size(); i++) {
      if (i > 0) {
        query.append(" AND ");
      }
      query.append(columns.get(i).name());
      query.append(" IS NOT NULL");
    }
    query.append(" PRIMARY KEY (");
    query.append(columnNameCompilation(columns.stream()
      .filter(column -> column.type().isPartitionKey()).toList(), "(", "),"));
    query.append(columnNameCompilation(columns.stream()
      .filter(column -> column.type().isClusteringKey()).toList(), "", ""));
    query.append(columnNameCompilation(columns.stream()
      .filter(column -> column.type().isPrimaryKey()).toList(), "", ""));
    query.append(");");
    connection.execute(query);
    var viewTableColumns = Lists.newArrayList(columns);
    viewTableColumns.addAll(this.columns.stream().filter(tableColumn ->
      columns.stream().noneMatch(viewColumn ->
        viewColumn.name().equalsIgnoreCase(tableColumn.name()))).toList());
    return new DatabaseTable(connection, keyspace, this.name + "_" + name,
      viewTableColumns);
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
    query.append(row.placeholderCompilation());
    query.append(") ");
    query.append(addition);
    query.append(";");
    return connection.execute(query, row.values()).thenApply(value -> null);
  }

  /**
   * Updates a row inside the database table
   * @param value The primary key value
   * @param row The updated row (with all the columns)
   * @return A future that is completed when the update is completed
   */
  protected CompletableFuture<Void> update(Object value, DatabaseRow row) {
    return update(DatabaseCondition.of(findPrimaryKeyColumn().name(), value), row);
  }

  /**
   * Updates a row inside the database table
   * @param condition The condition with which the row can be found
   * @param row The updated row (with all the columns)
   * @return A future that is completed when the update is completed
   */
  protected CompletableFuture<Void> update(
    DatabaseCondition condition, DatabaseRow row
  ) {
    return update(condition, row, false);
  }

  /**
   * Updates a row inside the database table
   * @param condition The condition with which the row can be found
   * @param row The updated row (with all the columns)
   * @param allowFiltering Whether filtering is allowed
   * @return A future that is completed when the update is completed
   */
  protected CompletableFuture<Void> update(
    DatabaseCondition condition, DatabaseRow row, boolean allowFiltering
  ) {
    var query = new StringBuilder("UPDATE ");
    query.append(fullName());
    query.append(" SET ");
    query.append(buildUpdateKeyValuePairs());
    query.append(" WHERE ");
    query.append(condition.build());
    if (allowFiltering) {
      query.append(" ALLOW FILTERING");
    }
    query.append(";");
    return connection.execute(query, row.values(), condition.values())
      .thenApply(value -> null);
  }

  private String buildUpdateKeyValuePairs() {
    var pairs = new StringBuilder();
    for (var i = 0; i < columns.size(); i++) {
      var column = columns.get(i);
      if (!column.type().isRegular()) {
        continue;
      }
      pairs.append(column.name());
      pairs.append(" = ?");
      if (i < columns.size() - 1) {
        pairs.append(", ");
      }
    }
    return pairs.toString();
  }

  /**
   * Is used to check whether a row inside the database table exists
   * @param value The primary key value
   * @return A future that contains the existence boolean
   */
  protected CompletableFuture<Boolean> exists(Object value) {
    if (value == null) {
      return CompletableFuture.completedFuture(false);
    }
    return exists(DatabaseCondition.of(findPrimaryKeyColumn().name(), value));
  }

  /**
   * Is used to check whether a row inside the database table exists
   * @param condition The condition with which the row can be found
   * @return A future that contains the existence boolean
   */
  protected CompletableFuture<Boolean> exists(DatabaseCondition condition) {
    return exists(condition, false);
  }

  /**
   * Is used to check whether a row inside the database table exists
   * @param condition The condition with which the row can be found
   * @param allowFiltering Whether filtering is allowed
   * @return A future that contains the existence boolean
   */
  protected CompletableFuture<Boolean> exists(
    DatabaseCondition condition, boolean allowFiltering
  ) {
    var query = new StringBuilder("SELECT ");
    query.append(columnNameCompilation());
    query.append(" FROM ");
    query.append(fullName());
    query.append(" WHERE ");
    query.append(condition.build());
    if (allowFiltering) {
      query.append(" ALLOW FILTERING");
    }
    query.append(";");
    return connection.execute(query, condition.values())
      .thenApply(result -> result.remaining() > 0);
  }

  /**
   * Is used to find the number of rows inside a database table
   * @return The number of rows
   */
  public CompletableFuture<Long> count() {
    var query = new StringBuilder("SELECT COUNT(*) FROM ");
    query.append(fullName());
    query.append(";");
    return connection.execute(query)
      .thenApply(result -> result.one().get(0, Long.class));
  }

  /**
   * Is used to find the number of rows inside a database table
   * @param condition The condition for counting
   * @return The number of rows
   */
  protected CompletableFuture<Long> count(DatabaseCondition condition) {
    return count(condition, false);
  }

  /**
   * Is used to find the number of rows inside a database table
   * @param condition The condition for counting
   * @param allowFiltering Whether filtering is allowed
   * @return The number of rows
   */
  protected CompletableFuture<Long> count(
    DatabaseCondition condition, boolean allowFiltering
  ) {
    var query = new StringBuilder("SELECT COUNT(*) FROM ");
    query.append(fullName());
    query.append(" WHERE ");
    query.append(condition.build());
    if (allowFiltering) {
      query.append(" ALLOW FILTERING");
    }
    query.append(";");
    return connection.execute(query, condition.values())
      .thenApply(result -> result.one().get(0, Long.class));
  }

  /**
   * Finds all available rows inside the database table
   * @return List of all possible rows
   */
  protected CompletableFuture<List<DatabaseRow>> selectAllRows() {
    var query = new StringBuilder("SELECT ");
    query.append(columnNameCompilation());
    query.append(" FROM ");
    query.append(fullName());
    query.append(";");
    return connection.execute(query).thenApply(result ->
      DatabaseRow.multiple(result.currentPage(), columns.size()));
  }

  /**
   * Is used to find a single row
   * @param value The primary key value
   * @return A future that contains the database row
   */
  protected CompletableFuture<DatabaseRow> selectRow(Object value) {
    return selectRow(DatabaseCondition.of(findPrimaryKeyColumn().name(), value));
  }

  /**
   * Is used to find a single row
   * @param condition The condition with which the row can be found
   * @return A future that contains the database row
   */
  protected CompletableFuture<DatabaseRow> selectRow(DatabaseCondition condition) {
    return selectRow(condition, false);
  }

  /**
   * Is used to find a single row
   * @param condition The condition with which the row can be found
   * @param allowFiltering Whether filtering is allowed
   * @return A future that contains the database row
   */
  protected CompletableFuture<DatabaseRow> selectRow(
    DatabaseCondition condition, boolean allowFiltering
  ) {
    var futureResponse = new CompletableFuture<DatabaseRow>();
    selectRows(condition, allowFiltering)
      .thenAccept(rows -> futureResponse.complete(rows.get(0)));
    return futureResponse;
  }

  /**
   * Is used to find a single row secured (optional result)
   * @param value The primary key value
   * @return A future that contains the database row
   */
  protected CompletableFuture<Optional<DatabaseRow>> selectRowSecure(Object value) {
    return selectRowSecure(DatabaseCondition.of(findPrimaryKeyColumn().name(), value));
  }

  /**
   * Is used to find a single row secured (optional result)
   * @param condition The condition with which the row can be found
   * @return A future that contains the database row
   */
  protected CompletableFuture<Optional<DatabaseRow>> selectRowSecure(
    DatabaseCondition condition
  ) {
    return selectRowSecure(condition, false);
  }

  /**
   * Is used to find a single row secured (optional result)
   * @param condition The condition with which the row can be found
   * @param allowFiltering Whether filtering is allowed
   * @return A future that contains the database row
   */
  protected CompletableFuture<Optional<DatabaseRow>> selectRowSecure(
    DatabaseCondition condition, boolean allowFiltering
  ) {
    var futureResponse = new CompletableFuture<Optional<DatabaseRow>>();
    selectRows(condition, allowFiltering).thenAccept(rows ->
      futureResponse.complete(rows.stream().findFirst()));
    return futureResponse;
  }

  /**
   * Is used to find a multiple rows
   * @param condition The condition with which the rows can be found
   * @return A future that contains the database rows
   */
  protected CompletableFuture<List<DatabaseRow>> selectRows(
    DatabaseCondition condition
  ) {
    return selectRows(condition, -1, false);
  }

  /**
   * Is used to find a multiple rows
   * @param condition The condition with which the rows can be found
   * @param limit The limit of entries that should be returned
   * @return A future that contains the database rows
   */
  protected CompletableFuture<List<DatabaseRow>> selectRows(
    DatabaseCondition condition, long limit
  ) {
    return selectRows(condition, limit, false);
  }

  /**
   * Is used to find a multiple rows
   * @param condition The condition with which the rows can be found
   * @param allowFiltering Whether filtering is allowed
   * @return A future that contains the database rows
   */
  protected CompletableFuture<List<DatabaseRow>> selectRows(
    DatabaseCondition condition, boolean allowFiltering
  ) {
    return selectRows(condition, -1, allowFiltering);
  }

  /**
   * Is used to find a multiple rows
   * @param condition The condition with which the rows can be found
   * @param limit The limit of entries that should be returned
   * @param allowFiltering Whether filtering is allowed
   * @return A future that contains the database rows
   */
  protected CompletableFuture<List<DatabaseRow>> selectRows(
    DatabaseCondition condition, long limit, boolean allowFiltering
  ) {
    var query = new StringBuilder("SELECT ");
    query.append(columnNameCompilation());
    query.append(" FROM ");
    query.append(fullName());
    query.append(" WHERE ");
    query.append(condition.build());
    if (limit > 0) {
      query.append(" LIMIT ");
      query.append(limit);
    }
    if (allowFiltering) {
      query.append(" ALLOW FILTERING");
    }
    query.append(";");
    return connection.execute(query, condition.values()).thenApply(result ->
      DatabaseRow.multiple(result.currentPage(), columns.size()));
  }

  /**
   * Used to find a specific page inside the table
   * @param partitionValue The partition key value that specifies the
   *                       basic set of elements to be paged
   * @param condition The condition that is used for filtering
   * @param order The direction in which sorting should take place
   * @param pageSize The page size that is used for the paging process
   * @param targetPage The page the requester wants to jump to
   * @return A future that contains the page
   */
  public CompletableFuture<DatabasePage<DatabaseRow>> selectPage(
    Object partitionValue, DatabaseCondition condition, DatabaseOrder order,
    int pageSize, int targetPage
  ) {
    return countPagingRows(partitionValue, condition).thenCompose(count ->
      selectPage(partitionValue, condition, order, pageSize, count, targetPage));
  }

  private CompletableFuture<DatabasePage<DatabaseRow>> selectPage(
    Object partitionValue, DatabaseCondition condition, DatabaseOrder order,
    int pageSize, long rowNumber, int targetPage
  ) {
    var pageNumber = calculatePageNumber(pageSize, rowNumber);
    if (targetPage != 0 && targetPage != pageNumber - 1) {
      return CompletableFuture.completedFuture(DatabasePage.empty());
    }
    var direction = targetPage == 0 ? DatabaseDirection.FORWARD :
      DatabaseDirection.BACKWARD;
    var pagingCondition = createPagingCondition(partitionValue, condition);
    var statement = createPagingStatement(pagingCondition,
      direction.isForward() ? order : order.reverse(), pageSize, "");
    if (targetPage == pageNumber - 1) {
      var offset = (int) (rowNumber % pageSize);
      statement = statement.setPageSize(offset == 0 ? pageSize : offset);
    }
    return connection.execute(statement).thenApply(result ->
      createDatabasePage(pageNumber, result, direction));
  }

  /**
   * Used to shift an existing paging state (next or previous page)
   * @param partitionValue The partition key value that specifies the
   *                       basic set of elements to be paged
   * @param condition The condition that is used for filtering
   * @param order The direction in which sorting should take place
   * @param pageSize The page size that is used for the paging process
   * @param pageState The current page state
   * @param startingPoint Whether you come from the back or from the front
   * @param direction The direction in which you want to shift
   * @return A future that contains the page
   */
  public CompletableFuture<DatabasePage<DatabaseRow>> shiftPage(
    Object partitionValue, DatabaseCondition condition, DatabaseOrder order,
    int pageSize, String pageState, DatabaseDirection startingPoint,
    DatabaseDirection direction
  ) {
    return countPagingRows(partitionValue, condition).thenCompose(count ->
      shiftPage(partitionValue, condition, order,
        pageSize, count, pageState, startingPoint, direction));
  }

  private CompletableFuture<DatabasePage<DatabaseRow>> shiftPage(
    Object partitionValue, DatabaseCondition condition, DatabaseOrder order,
    int pageSize, long rowNumber, String pageState, DatabaseDirection startingPoint,
    DatabaseDirection direction
  ) {
    var pageNumber = calculatePageNumber(pageSize, rowNumber);
    var pagingCondition = createPagingCondition(partitionValue, condition);
    var statement = createPagingStatement(pagingCondition,
      direction.isForward() ? order : order.reverse(), pageSize, pageState);
    return connection.execute(statement, pagingCondition.values()).thenCompose(result ->
      findShiftedPage(pageSize, pageNumber, result, startingPoint, direction));
  }

  private CompletableFuture<DatabasePage<DatabaseRow>> findShiftedPage(
    int pageSize, int pageNumber, AsyncResultSet firstResult,
    DatabaseDirection startingPoint, DatabaseDirection direction
  ) {
    if (startingPoint == direction) {
      return CompletableFuture.completedFuture(createDatabasePage(pageNumber,
        firstResult, direction));
    }
    return (CompletableFuture<DatabasePage<DatabaseRow>>)
      firstResult.fetchNextPage().thenApply(secondResult ->
        createDatabasePage(pageNumber, firstResult,
          combineShiftResults(pageSize, firstResult, secondResult), direction));
  }

  private List<Row> combineShiftResults(
    int pageSize, AsyncResultSet firstResult,
    AsyncResultSet secondResult
  ) {
    var firstBlock = Lists.newArrayList(firstResult.currentPage())
      .stream().sorted((a, b) -> -1).limit(1).sorted((a, b) -> -1).toList();
    var secondBlock = Lists.newArrayList(secondResult.currentPage()).stream()
      .limit(pageSize - 1).toList();
    var result = Lists.<Row>newArrayList();
    result.addAll(firstBlock);
    result.addAll(secondBlock);
    return result;
  }

  private DatabasePage<DatabaseRow> createDatabasePage(
    int pageNumber, AsyncResultSet resultSet, DatabaseDirection direction
  ) {
    return createDatabasePage(pageNumber, resultSet,
      Lists.newArrayList(resultSet.currentPage()), direction);
  }

  private DatabasePage<DatabaseRow> createDatabasePage(
    int pageNumber, AsyncResultSet resultSet, List<Row> rows,
    DatabaseDirection direction
  ) {
    if (direction.isBackward()) {
      Collections.reverse(rows);
    }
    return DatabasePage.create(DatabaseRow.multiple(rows, columns.size()),
      resultSet.hasMorePages() ?
        resultSet.getExecutionInfo().getSafePagingState().toString() : "",
      pageNumber);
  }

  private SimpleStatement createPagingStatement(
    DatabaseCondition condition, DatabaseOrder order, int pageSize,
    String pageState
  ) {
    var query = new StringBuilder("SELECT ");
    query.append(columnNameCompilation());
    query.append(" FROM ");
    query.append(fullName());
    query.append(" WHERE ");
    query.append(condition.build());
    query.append(" ORDER BY ");
    query.append(columns.stream().filter(column -> column.type().isClusteringKey())
      .findFirst().get().name());
    query.append(" ");
    query.append(order.value());
    query.append(" ALLOW FILTERING;");
    var statement = SimpleStatement.builder(query.toString())
      .setPageSize(pageSize).build();
    if (!pageState.isEmpty()) {
      statement = statement.setPagingState(PagingState.fromString(pageState)
        .getRawPagingState());
    }
    return statement;
  }

  private CompletableFuture<Long> countPagingRows(
    Object partitionValue, DatabaseCondition condition
  ) {
    return count(createPagingCondition(partitionValue, condition), true);
  }

  private DatabaseCondition createPagingCondition(
    Object partitionValue, DatabaseCondition condition
  ) {
    var finalCondition = DatabaseCondition.of(findPartitionKeyColumn().name(),
      partitionValue);
    finalCondition.concat(condition);
    return finalCondition;
  }

  private int calculatePageNumber(int pageSize, long rowNumber) {
    return (int) Math.ceil(((double) rowNumber) / pageSize);
  }

  /**
   * Deletes a database row from the database table
   * @param value The primary key value
   * @return A future that is completed when the deletion is completed
   */
  protected CompletableFuture<Void> delete(Object value) {
    return delete(DatabaseCondition.of(findPrimaryKeyColumn().name(), value));
  }

  /**
   * Deletes a database row from the database table
   * @param condition The condition with which the rows can be found
   * @return A future that is completed when the deletion is completed
   */
  protected CompletableFuture<Void> delete(DatabaseCondition condition) {
    var query = new StringBuilder("DELETE FROM ");
    query.append(fullName());
    query.append(" WHERE ");
    query.append(condition.build());
    query.append(";");
    return connection.execute(query, condition.values()).thenApply(value -> null);
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

  protected void drop(String addition) {
    var query = new StringBuilder("DROP TABLE ");
    query.append(addition);
    query.append(fullName());
    query.append(";");
    connection.execute(query);
  }

  /**
   * Deletes the materialized view and all its content
   */
  public void dropMaterializedView() {
    dropMaterializedView("");
  }

  /**
   * Deletes the materialized view and all its content only if it exists
   */
  public void dropMaterializedViewIfExists() {
    dropMaterializedView("IF EXISTS ");
  }

  private void dropMaterializedView(String addition) {
    var query = new StringBuilder("DROP MATERIALIZED VIEW ");
    query.append(addition);
    query.append(fullName());
    query.append(";");
    connection.execute(query);
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

  private DatabaseColumn findPrimaryKeyColumn() {
    return columns.stream().filter(column -> column.type().isPrimaryKey())
      .findFirst().get();
  }

  private List<DatabaseColumn> findPrimaryKeyColumns() {
    return columns.stream().filter(column -> column.type().isPrimaryKey())
      .toList();
  }

  private DatabaseColumn findPartitionKeyColumn() {
    return columns.stream().filter(column -> column.type().isPartitionKey())
      .findFirst().get();
  }

  private List<DatabaseColumn> findPartitionKeyColumns() {
    return columns.stream().filter(column -> column.type().isPartitionKey())
      .toList();
  }

  /**
   * Is used by table system (databases) to configure table asynchronous
   * @param newColumns The columns of the table
   */
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
