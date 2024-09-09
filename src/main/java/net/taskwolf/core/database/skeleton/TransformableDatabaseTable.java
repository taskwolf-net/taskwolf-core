package net.taskwolf.core.database.skeleton;

import com.datastax.oss.driver.api.core.cql.Row;
import com.google.common.collect.Lists;
import net.taskwolf.core.database.DatabaseColumn;
import net.taskwolf.core.database.DatabaseDataType;
import net.taskwolf.core.database.DatabaseListColumn;
import net.taskwolf.core.database.DatabaseTable;
import net.taskwolf.core.database.transformation.DatabaseTransformation;
import net.taskwolf.core.database.transformation.DatabaseTransformationState;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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
    return CompletableFuture.completedFuture(DatabaseTransformationState.USE_TEMPORARY);
  }

  /**
   * Uses the temporary table for operation only
   * @return A future that then next transformation state
   */
  default CompletableFuture<DatabaseTransformationState> useTemporaryTable() {
    return drop().thenCompose(dropValue -> create()
      .thenApply(createValue -> DatabaseTransformationState.FILL_NEW));
  }

  /**
   * Transforms the data of the temporary table to the new table
   * @return A future that then next transformation state
   */
  default CompletableFuture<DatabaseTransformationState> fillNewTable() {
    return CompletableFuture.completedFuture(DatabaseTransformationState.USE_NEW);
  }

  /**
   * Uses the new table for operation only
   * @return A future that then next transformation state
   */
  default CompletableFuture<DatabaseTransformationState> useNewTable() {
    return temporaryTable().drop()
      .thenApply(dropValue -> DatabaseTransformationState.INACTIVE);
  }

  /**
   * Is used to check if a table discrepancy is present
   */
  default CompletableFuture<Boolean> checkTableDiscrepancy() {
    if (transformation() == null) {
      return CompletableFuture.completedFuture(false);
    }
    return findTableColumns().thenApply(this::checkTableDiscrepancy);
  }

  private boolean checkTableDiscrepancy(List<DatabaseColumn> previousColumns) {
    var originColumns = findOriginColumns(previousColumns);
    if (originColumns.isEmpty()) {
      return false;
    }
    var temporaryTable = new DatabaseTable(connection(), keyspace(),
      name() + "_tmp", originColumns.get());
    temporaryTable.createIfNotExists();
    equipTemporaryTable(temporaryTable);
    return true;
  }

  private Optional<List<DatabaseColumn>> findOriginColumns(
    List<DatabaseColumn> previousColumns
  ) {
    if (previousColumns.isEmpty()) {
      return Optional.empty();
    }
    var columns = columns();
    if (previousColumns.size() != columns.size()) {
      return Optional.of(previousColumns);
    }
    for (var i = 0; i < columns.size(); i++) {
      if (!columns.get(i).equals(previousColumns.get(i))) {
        return Optional.of(previousColumns);
      }
    }
    return Optional.empty();
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
