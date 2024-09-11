package net.taskwolf.core.database.skeleton;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.DatabaseColumn;
import net.taskwolf.core.database.DatabaseTable;

import java.util.List;

public interface ViewableDatabaseTable extends AbstractDatabaseTable {
  /**
   * Creates a new materialized view from the table
   * @param name The name of the materialized view
   * @param columns The column settings (primary, partition and clustering columns)
   * @return The materialized view table
   */
  default DatabaseTable createMaterializedView(
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
  default DatabaseTable createMaterializedViewIfNotExists(
    String name, String clusteringColumnName
  ) {
    var primaryColumns = Lists.newArrayList(columns().stream()
      .filter(column -> column.type().isPartitionKey()).toList());
    primaryColumns.add(columns().stream()
      .filter(column -> column.name().equalsIgnoreCase(clusteringColumnName))
      .map(column -> DatabaseColumn.create(column.name(), column.dataType(),
        DatabaseColumn.Type.CLUSTERING_KEY)).findFirst().get());
    primaryColumns.addAll(columns().stream()
      .filter(column -> column.type().isClusteringKey()).toList());
    return createMaterializedViewIfNotExists(name, primaryColumns);
  }

  /**
   * Creates a new materialized view from the table
   * @param name The name of the materialized view
   * @param columns The column settings (primary, partition and clustering columns)
   * @return The materialized view table
   */
  default DatabaseTable createMaterializedViewIfNotExists(
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
    connection().execute(query).join();
    var viewTableColumns = Lists.newArrayList(columns);
    viewTableColumns.addAll(this.columns().stream().filter(tableColumn ->
      columns.stream().noneMatch(viewColumn ->
        viewColumn.name().equalsIgnoreCase(tableColumn.name()))).toList());
    return new DatabaseTable(connection(), keyspace(), this.name() + "_" + name,
      viewTableColumns);
  }

  /**
   * Deletes the materialized view and all its content
   */
  default void dropMaterializedView() {
    dropMaterializedView("");
  }

  /**
   * Deletes the materialized view and all its content only if it exists
   */
  default void dropMaterializedViewIfExists() {
    dropMaterializedView("IF EXISTS ");
  }

  private void dropMaterializedView(String addition) {
    var query = new StringBuilder("DROP MATERIALIZED VIEW ");
    query.append(addition);
    query.append(fullName());
    query.append(";");
    connection().execute(query);
  }
}
