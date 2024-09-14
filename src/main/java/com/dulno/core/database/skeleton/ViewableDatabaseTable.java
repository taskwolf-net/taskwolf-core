package com.dulno.core.database.skeleton;

import com.datastax.oss.driver.api.core.metadata.schema.ViewMetadata;
import com.dulno.core.database.DatabaseColumn;
import com.dulno.core.iterator.AsyncIterator;
import com.google.common.collect.Lists;
import com.dulno.core.database.DatabaseTable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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
      .filter(column -> column.type().isPartitionKey() || column.type().isPrimaryKey())
      .toList());
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
      .filter(column -> column.type().isPartitionKey() || column.type().isPrimaryKey())
      .toList(), "(", "),"));
    query.append(columnNameCompilation(columns.stream()
      .filter(column -> column.type().isClusteringKey()).toList(), "", ""));
    query.append(");");
    connection().executesSynchronously(query);
    var viewTableColumns = Lists.newArrayList(columns);
    viewTableColumns.addAll(this.columns().stream().filter(tableColumn ->
      columns.stream().noneMatch(viewColumn ->
        viewColumn.name().equalsIgnoreCase(tableColumn.name()))).toList());
    return new DatabaseTable(connection(), keyspace(), this.name() + "_" + name,
      viewTableColumns);
  }

  /**
   * Is used to delete all registered view
   * @return A future that is completed when the deletion is completed
   */
  default CompletableFuture<Void> dropAllViews() {
    var keyspaceMetadata = connection().metadata().getKeyspace(keyspace().name())
      .orElseThrow();
    var tableMetadata = keyspaceMetadata.getTable(name());
    if (tableMetadata.isEmpty()) {
      return CompletableFuture.completedFuture(null);
    }
    var views = Lists.<ViewMetadata>newArrayList();
    for (var view : keyspaceMetadata.getViews().values()) {
      if (view.getBaseTable().equals(tableMetadata.get().getName())) {
        views.add(view);
      }
    }
    return AsyncIterator.execute(views,
        view -> dropMaterializedView(view.getKeyspace().asInternal(),
          view.getName().asInternal()))
      .thenApply(value -> null);
  }

  /**
   * Deletes the materialized view and all its content
   * @return A future that is completed when the deletion is completed
   */
  default CompletableFuture<Void> dropMaterializedView() {
    return dropMaterializedView("");
  }

  /**
   * Deletes the materialized view and all its content only if it exists
   * @return A future that is completed when the deletion is completed
   */
  default CompletableFuture<Void> dropMaterializedViewIfExists() {
    return dropMaterializedView("IF EXISTS ");
  }

  private CompletableFuture<Void> dropMaterializedView(String addition) {
    var query = new StringBuilder("DROP MATERIALIZED VIEW ");
    query.append(addition);
    query.append(fullName());
    query.append(";");
    return connection().execute(query).thenApply(value -> null);
  }

  /**
   * Deletes the materialized view and all its content
   * @return A future that is completed when the deletion is completed
   */
  default CompletableFuture<Void> dropMaterializedView(
    String keyspaceName, String viewName
  ) {
    return dropMaterializedView(keyspaceName, viewName, "");
  }

  /**
   * Deletes the materialized view and all its content only if it exists
   * @return A future that is completed when the deletion is completed
   */
  default CompletableFuture<Void> dropMaterializedViewIfExists(
    String keyspaceName, String viewName
  ) {
    return dropMaterializedView(keyspaceName, viewName, "IF EXISTS ");
  }

  private CompletableFuture<Void> dropMaterializedView(
    String keyspaceName, String viewName, String addition
  ) {
    var query = new StringBuilder("DROP MATERIALIZED VIEW ");
    query.append(addition);
    query.append(keyspaceName + "." + viewName);
    query.append(";");
    return connection().execute(query).thenApply(value -> null);
  }
}
