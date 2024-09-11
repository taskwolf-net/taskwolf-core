package net.taskwolf.core.database.skeleton;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;
import net.taskwolf.core.database.transformation.DatabaseTransformation;
import net.taskwolf.core.database.transformation.DatabaseTransformationState;
import net.taskwolf.core.iterator.AsyncIterator;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface TransformableDatabaseTable extends AbstractDatabaseTable,
  CreatableDatabaseTable, DroppableDatabaseTable
{
  /**
   * The structure used for swapping between old and new format
   * @return The transformation
   */
  DatabaseTransformation transformation();

  /**
   * The state that is used to determine whether there is a transformation
   * currently running
   * @return The transformation state
   */
  DatabaseTransformationState transformationState();

  /**
   * The temporary database table that will be used in the transformation process
   * @return The temporary database table
   */
  DatabaseTable temporaryTable();

  /**
   * Is used to set the temporary database table
   * @param temporaryTable The temporary database table
   */
  void equipTemporaryTable(DatabaseTable temporaryTable);

  /**
   * Is used to update the current transformation state of the table
   * @param newState The new transformation state
   */
  void updateTransformationState(DatabaseTransformationState newState);

  /**
   * Moves the data of the origin table to the temporary table
   * @return A future that then next transformation state
   */
  default CompletableFuture<DatabaseTransformationState> fillTemporaryTable() {
    return transformData(table(), temporaryTable(),
      CompletableFuture::completedFuture)
      .thenApply(success -> success ? DatabaseTransformationState.USE_TEMPORARY :
        DatabaseTransformationState.FAILURE);
  }

  /**
   * Uses the temporary table for operation only
   * @return A future that then next transformation state
   */
  default CompletableFuture<DatabaseTransformationState> useTemporaryTable() {
    return drop().thenCompose(dropValue -> createAsyncIfNotExists()
      .thenAccept(createValue -> transformation().initializeNewTable(table()))
      .thenApply(initializedValue -> DatabaseTransformationState.FILL_NEW));
  }

  /**
   * Transforms the data of the temporary table to the new table
   * @return A future that then next transformation state
   */
  default CompletableFuture<DatabaseTransformationState> fillNewTable() {
    return createAsyncIfNotExists()
      .thenAccept(createValue -> transformation().initializeNewTable(table()))
      .thenCompose(initializedValue -> transformData(temporaryTable(), table(),
        transformation()::transformOldToNew)
        .thenApply(success -> success ? DatabaseTransformationState.USE_NEW :
          DatabaseTransformationState.FAILURE));
  }

  /**
   * Uses the new table for operation only
   * @return A future that then next transformation state
   */
  default CompletableFuture<DatabaseTransformationState> useNewTable() {
    return temporaryTable().drop()
      .thenApply(dropValue -> DatabaseTransformationState.INACTIVE);
  }

  private CompletableFuture<Boolean> transformData(
    DatabaseTable origin, DatabaseTable destination,
    Function<DatabaseRow, CompletableFuture<DatabaseRow>> transformation
  ) {
    var query = new StringBuilder("SELECT ");
    query.append(columnNameCompilation(origin.columns(), "", ""));
    query.append(" FROM ");
    query.append(origin.fullName());
    query.append(";");
    var futureResponse = new CompletableFuture<Boolean>();
    var statement = SimpleStatement.builder(query.toString())
      .setPageSize(10).build();
    Runnable callback = () -> checkTransformationSuccess(origin, destination)
      .thenAccept(futureResponse::complete);
    connection().execute(statement)
      .thenAccept(result -> processPageTransformationData(origin,
        destination, transformation, result, callback));
    return futureResponse;
  }

  private void processPageTransformationData(
    DatabaseTable origin, DatabaseTable destination,
    Function<DatabaseRow, CompletableFuture<DatabaseRow>> transformation,
    AsyncResultSet result, Runnable callback
  ) {
    var rows = DatabaseRow.multiple(result.currentPage(), origin.columns().size());
    AsyncIterator.execute(rows, transformation::apply)
      .thenAccept(transformedRows -> AsyncIterator.execute(transformedRows,
        row -> insertTransformedRow(origin, destination, row))
        .thenAccept(value -> finishPageTransformation(origin, destination,
          transformation, result, callback)));
  }

  private CompletableFuture<Void> insertTransformedRow(
    DatabaseTable origin, DatabaseTable destination, DatabaseRow row
  ) {
    return destination.existsFix(row).thenCompose(destinationExists ->
      destinationExists ? CompletableFuture.completedFuture(null) :
        origin.existsFix(row).thenCompose(originExists -> !originExists ?
          CompletableFuture.completedFuture(null) : destination.insertFix(row)));
  }

  private void finishPageTransformation(
    DatabaseTable origin, DatabaseTable destination,
    Function<DatabaseRow, CompletableFuture<DatabaseRow>> transformation,
    AsyncResultSet result, Runnable callback
  ) {
    if (result.hasMorePages()) {
      result.fetchNextPage().thenAccept(nextPage -> processPageTransformationData(
        origin, destination, transformation, nextPage, callback));
      return;
    }
    callback.run();
  }

  private CompletableFuture<Boolean> checkTransformationSuccess(
    DatabaseTable origin, DatabaseTable destination
  ) {
    return origin.countFix()
      .thenCompose(originCount -> destination.countFix()
        .thenApply(destinationCount -> originCount == destinationCount));
  }

  /**
   * Is used to check if a table discrepancy is present
   */
  default CompletableFuture<Boolean> checkTableDiscrepancy() {
    if (transformation() == null) {
      return CompletableFuture.completedFuture(false);
    }
    return findTableColumns(keyspace().name(), name())
      .thenCompose(this::checkTableDiscrepancy);
  }

  private CompletableFuture<Boolean> checkTableDiscrepancy(
    List<DatabaseColumn> currentColumns
  ) {
    if (connection().tableExists(table()) &&
      checkColumnMatch(columns(), currentColumns)
    ) {
      return CompletableFuture.completedFuture(false);
    }
    var temporaryTable = new DatabaseTable(connection(), keyspace(),
      name() + "_tmp", transformation().oldColumns());
    equipTemporaryTable(temporaryTable);
    determineInitialState();
    if (!transformationState().isInactive()) {
      return CompletableFuture.completedFuture(true);
    }
    return temporaryTable.createAsyncIfNotExists()
      .thenApply(value -> setupTemporaryTable(temporaryTable));
  }

  private boolean setupTemporaryTable(DatabaseTable temporaryTable) {
    var keyspaceMetadata = connection().metadata().getKeyspace(keyspace().name())
      .orElseThrow();
    var tableMetadata = keyspaceMetadata.getTable(name()).orElseThrow();
    for (var index : tableMetadata.getIndexes().values()) {
      var query = index.describe(false)
        .replace("ON \"" + keyspace().name() + "\".\"" + name() + "\"",
          "ON \"" + keyspace().name() + "\".\"" + temporaryTable.name() + "\"")
        .replace("_idx\" ON", "_tmp_idx\" ON");
      connection().execute(query).join();
    }
    for (var view : keyspaceMetadata.getViews().values()) {
      if (view.getBaseTable().equals(tableMetadata.getName())) {
        //TODO: IMPLEMENT MATERIALIZED VIEW CREATION
        var query = view.describe(false)
          .replace("ON \"" + keyspace().name() + "\".\"" + name() + "\"",
            "ON \"" + keyspace().name() + "\".\"" + temporaryTable.name() + "\"");
        connection().execute(query).join();
      }
    }
    return true;
  }

  private void determineInitialState() {
    var currentExists = connection().tableExists(keyspace().name(), name());
    var temporaryExists = connection().tableExists(keyspace().name(), name() + "_tmp");
    if (currentExists && !temporaryExists) {
      updateTransformationState(DatabaseTransformationState.INACTIVE);
      return;
    }
    if (temporaryExists && !currentExists) {
      updateTransformationState(DatabaseTransformationState.USE_TEMPORARY);
      return;
    }
    var currentColumns = findTableColumns(keyspace().name(), name()).join();
    var temporaryColumns = findTableColumns(keyspace().name(), name() + "_tmp").join();
    if (checkColumnMatch(currentColumns, temporaryColumns)) {
      updateTransformationState(DatabaseTransformationState.FILL_TEMPORARY);
    } else {
      updateTransformationState(DatabaseTransformationState.FILL_NEW);
    }
  }

  default CompletableFuture<AsyncResultSet> executeTableCreateQuery(
    StringBuilder query
  ) {
    if (transformation() != null && transformationState().isInactive()) {
      return CompletableFuture.completedFuture(null);
    }
    return connection().execute(query);
  }

  private boolean checkColumnMatch(
    List<DatabaseColumn> firstColumns, List<DatabaseColumn> secondColumns
  ) {
    if (firstColumns.size() != secondColumns.size()) {
      return false;
    }
    var firstCopy = Lists.newArrayList(firstColumns);
    var secondCopy = Lists.newArrayList(secondColumns);
    firstCopy.sort(Comparator.comparing(DatabaseColumn::name));
    secondCopy.sort(Comparator.comparing(DatabaseColumn::name));
    for (var i = 0; i < firstCopy.size(); i++) {
      if (!firstCopy.get(i).equals(secondCopy.get(i))) {
        return false;
      }
    }
    return true;
  }

  private CompletableFuture<List<DatabaseColumn>> findTableColumns(
    String keyspaceName, String tableName
  ) {
    var query = new StringBuilder("SELECT * FROM system_schema.columns WHERE ");
    query.append("keyspace_name = '");
    query.append(keyspaceName);
    query.append("' AND table_name = '");
    query.append(tableName);
    query.append("';");
    return connection().execute(query).thenApply(result -> result.remaining() > 0 ?
      createDatabaseColumns(result.currentPage()) : Lists.newArrayList());
  }

  private List<DatabaseColumn> createDatabaseColumns(Iterable<Row> rows) {
    var partitionKeyColumns = Lists.<DatabaseColumn>newArrayList();
    var clusteringKeyColumns = Lists.<DatabaseColumn>newArrayList();
    var columns = Lists.<DatabaseColumn>newArrayList();
    for (var row : rows) {
      var column = createDatabaseColumnEntry(row);
      if (column.type().isPartitionKey()) {
        partitionKeyColumns.add(column);
      } else if (column.type().isClusteringKey()) {
        clusteringKeyColumns.add(column);
      } else {
        columns.add(column);
      }
    }
    if (partitionKeyColumns.size() == 1 && clusteringKeyColumns.isEmpty()) {
      var partitionKeyColumn = partitionKeyColumns.get(0);
      partitionKeyColumn.updateType(DatabaseColumn.Type.PRIMARY_KEY);
      columns.add(0, partitionKeyColumn);
      return columns;
    }
    columns.addAll(0, clusteringKeyColumns);
    columns.addAll(0, partitionKeyColumns);
    return columns;
  }

  private DatabaseColumn createDatabaseColumnEntry(Row row) {
    var columnName = row.getString("column_name");
    var kind = row.getString("kind");
    var columnType = switch (kind) {
      case "partition_key" -> DatabaseColumn.Type.PARTITION_KEY;
      case "clustering" -> DatabaseColumn.Type.CLUSTERING_KEY;
      default -> DatabaseColumn.Type.REGULAR;
    };
    var dataType = row.getString("type").toUpperCase();
    if (dataType.contains("LIST")) {
      return DatabaseListColumn.create(columnName, DatabaseDataType.valueOf(
          dataType.replace("LIST", "").replace("<", "").replace(">", "")),
        columnType);
    }
    return DatabaseColumn.create(columnName, DatabaseDataType.valueOf(dataType),
      columnType);
  }
}
