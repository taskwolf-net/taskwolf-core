package net.taskwolf.core.database.skeleton;

import net.taskwolf.core.database.DatabaseRow;

import java.util.concurrent.CompletableFuture;

public interface InsertableDatabaseTable extends AbstractDatabaseTable,
  TransformableDatabaseTable
{
  /**
   * Inserts a new database row into the database table
   * @param row The database row that is to be inserted
   * @return A future that is completed when the insertion is completed
   */
  default CompletableFuture<Void> insert(DatabaseRow row) {
    return insert(row, "");
  }

  /**
   * Inserts a new database row into the database table
   * @param row The database row that is to be inserted
   * @param addition An addition insertion argument (for example for ttl)
   * @return A future that is completed when the insertion is completed
   */
  default CompletableFuture<Void> insert(DatabaseRow row, String addition) {
    var query = new StringBuilder("INSERT INTO ");
    query.append(fullName());
    query.append(" (");
    query.append(columnNameCompilation());
    query.append(") VALUES (");
    query.append(row.placeholderCompilation());
    query.append(") ");
    query.append(addition);
    query.append(";");
    return connection().execute(query, row.values()).thenApply(value -> null);
  }
}
