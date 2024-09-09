package net.taskwolf.core.database.skeleton;

import net.taskwolf.core.database.DatabaseColumn;

import java.util.concurrent.CompletableFuture;

public interface CreatableDatabaseTable extends AbstractDatabaseTable {
  /**
   * Creates the database table even if it already exists
   */
  default CompletableFuture<Void> create() {
    return create("");
  }

  /**
   * Creates the database table only if it does not already exist
   */
  default CompletableFuture<Void> createIfNotExists() {
    return create("IF NOT EXISTS ");
  }

  private CompletableFuture<Void> create(String addition) {
    var query = new StringBuilder("CREATE TABLE ");
    query.append(addition);
    query.append(fullName());
    query.append(" (");
    query.append(columnCompilation());
    query.append(")");
    query.append(clusteringOrder());
    query.append(";");
    registerTable();
    return connection().execute(query).thenApply(value -> null);
  }

  /**
   * When this function is called the table will be registered in the keyspace
   */
  void registerTable();

  private String columnCompilation() {
    var compilation = new StringBuilder();
    for (var i = 0; i < columns().size(); i++) {
      compilation.append(columns().get(i).databaseEntry());
      compilation.append(", ");
    }
    compilation.append("PRIMARY KEY (");
    compilation.append(columnNameCompilation(columns().stream()
      .filter(column -> column.type().isPartitionKey()).toList(), "(", "),"));
    compilation.append(columnNameCompilation(columns().stream()
      .filter(column -> column.type().isClusteringKey()).toList(), "", ""));
    compilation.append(columnNameCompilation(columns().stream()
      .filter(column -> column.type().isPrimaryKey()).toList(), "", ""));
    compilation.append(")");
    return compilation.toString();
  }

  private String clusteringOrder() {
    var order = columns().stream().filter(DatabaseColumn::hasOrder).toList();
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
}
