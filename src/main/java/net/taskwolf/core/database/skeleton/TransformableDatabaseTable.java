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
    return transformData(table(), temporaryTable().columns(), temporaryTable(),
      CompletableFuture::completedFuture)
      .thenApply(success -> success ? DatabaseTransformationState.USE_TEMPORARY :
        DatabaseTransformationState.FAILURE);
  }

  /**
   * Uses the temporary table for operation only
   * @return A future that then next transformation state
   */
  default CompletableFuture<DatabaseTransformationState> useTemporaryTable() {
    return drop().thenCompose(dropValue -> createAsync()
      .thenApply(createValue -> DatabaseTransformationState.FILL_NEW));
  }

  /**
   * Transforms the data of the temporary table to the new table
   * @return A future that then next transformation state
   */
  default CompletableFuture<DatabaseTransformationState> fillNewTable() {
    return transformData(temporaryTable(), table(),
      transformation()::transformOldToNew)
      .thenApply(success -> success ? DatabaseTransformationState.USE_NEW :
        DatabaseTransformationState.FAILURE);
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
    return transformData(origin, origin.columns(), destination, transformation);
  }

  private CompletableFuture<Boolean> transformData(
    DatabaseTable origin, List<DatabaseColumn> originColumns,
    DatabaseTable destination,
    Function<DatabaseRow, CompletableFuture<DatabaseRow>> transformation
  ) {
    var query = new StringBuilder("SELECT ");
    query.append(columnNameCompilation(originColumns, "", ""));
    query.append(" FROM ");
    query.append(origin.fullName());
    query.append(";");
    var futureResponse = new CompletableFuture<Boolean>();
    var statement = SimpleStatement.builder(query.toString())
      .setPageSize(1).build();
    Runnable callback = () -> checkTransformationSuccess(origin, destination)
      .thenAccept(futureResponse::complete);
    connection().execute(statement)
      .thenAccept(result -> processPageTransformationData(origin, originColumns,
        destination, transformation, result, callback));
    return futureResponse;
  }

  private void processPageTransformationData(
    DatabaseTable origin, List<DatabaseColumn> originColumns,
    DatabaseTable destination,
    Function<DatabaseRow, CompletableFuture<DatabaseRow>> transformation,
    AsyncResultSet result, Runnable callback
  ) {
    try {
      Thread.sleep(10000);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    //TODO: CHECK EXISTENCE BEFORE INSERTING
    var rows = DatabaseRow.multiple(result.currentPage(), originColumns.size());
    AsyncIterator.execute(rows, transformation::apply).thenAccept(transformedRows ->
      AsyncIterator.execute(transformedRows, destination::insertFix).thenAccept(value ->
        finishPageTransformation(origin, originColumns, destination,
          transformation, result, callback)));
  }

  private void finishPageTransformation(
    DatabaseTable origin, List<DatabaseColumn> originColumns,
    DatabaseTable destination,
    Function<DatabaseRow, CompletableFuture<DatabaseRow>> transformation,
    AsyncResultSet result, Runnable callback
  ) {
    if (result.hasMorePages()) {
      result.fetchNextPage().thenAccept(nextPage -> processPageTransformationData(
        origin, originColumns, destination, transformation, nextPage, callback));
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
    return findTableColumns().thenCompose(this::checkTableDiscrepancy);
  }

  private CompletableFuture<Boolean> checkTableDiscrepancy(
    List<DatabaseColumn> currentColumns
  ) {
    if (checkColumnMatch(Lists.newArrayList(currentColumns))) {
      return CompletableFuture.completedFuture(false);
    }
    var temporaryTable = new DatabaseTable(connection(), keyspace(),
      name() + "_tmp", transformation().oldColumns());
    equipTemporaryTable(temporaryTable);
    return temporaryTable.createAsyncIfNotExists().thenApply(value -> true);
  }

  private boolean checkColumnMatch(
    List<DatabaseColumn> currentColumns
  ) {
    currentColumns.sort(Comparator.comparing(DatabaseColumn::name));
    var columns = columns().stream()
      .sorted(Comparator.comparing(DatabaseColumn::name)).toList();
    if (currentColumns.size() != columns.size()) {
      return false;
    }
    for (var i = 0; i < columns.size(); i++) {
      if (!columns.get(i).equals(currentColumns.get(i))) {
        return false;
      }
    }
    return true;
  }

  private CompletableFuture<List<DatabaseColumn>> findTableColumns() {
    var query = new StringBuilder("SELECT * FROM system_schema.columns WHERE ");
    query.append("keyspace_name = '");
    query.append(keyspace().name());
    query.append("' AND table_name = '");
    query.append(name());
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
