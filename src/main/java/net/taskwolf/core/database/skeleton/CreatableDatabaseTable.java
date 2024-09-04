package net.taskwolf.core.database.skeleton;

import net.taskwolf.core.database.DatabaseColumn;

public interface CreatableDatabaseTable extends AbstractDatabaseTable {
  /**
   * Creates the database table even if it already exists
   */
  default void create() {
    create("");
  }

  /**
   * Creates the database table only if it does not already exist
   */
  default void createIfNotExists() {
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
    connection().execute(query);
  }

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
