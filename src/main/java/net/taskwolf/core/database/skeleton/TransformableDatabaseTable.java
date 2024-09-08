package net.taskwolf.core.database.skeleton;

import com.datastax.oss.driver.api.core.cql.Row;
import com.google.common.collect.Lists;
import net.taskwolf.core.database.DatabaseColumn;
import net.taskwolf.core.database.DatabaseDataType;
import net.taskwolf.core.database.DatabaseListColumn;
import net.taskwolf.core.database.DatabaseTable;
import net.taskwolf.core.database.transformation.DatabaseTransformation;
import net.taskwolf.core.database.transformation.DatabaseTransformationStatus;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface TransformableDatabaseTable extends AbstractDatabaseTable {
  /**
   * The structure used for swapping between old and new format
   * @return The transformation
   */
  DatabaseTransformation transformation();

  /**
   * The status that is used to determine whether there is a transformation
   * currently running
   * @return The transformation status
   */
  DatabaseTransformationStatus transformationStatus();

  /**
   * The old / original database table that will now be transformed
   * @return The old database table
   */
  DatabaseTable transformationOrigin();

  /**
   * Is used to set the origin database table
   * @param origin The origin database table
   */
  void equipTransformationOrigin(DatabaseTable origin);

  /**
   * When this function is called, the transformation from the old table and
   * the old format to the new table and the new format is started and executed
   */
  default void transform() {

  }

  /**
   * Is used to check if a table discrepancy is present
   */
  default void checkTableDiscrepancy() {
    try {
      var originColumns = findOriginColumns();
      if (originColumns.isEmpty()) {
        return;
      }
      equipTransformationOrigin(new DatabaseTable(connection(), keyspace(), name(),
        originColumns.get()));
      //TODO: CHANGE THIS TABLE NAME (INCREMENT BY ONE)
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  private Optional<List<DatabaseColumn>> findOriginColumns() throws Exception {
    var previousColumns = findTableColumns().get();
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
