package net.taskwolf.core.database.skeleton;

import net.taskwolf.core.database.condition.DatabaseCondition;

import java.util.concurrent.CompletableFuture;

public interface DeletableDatabaseTable extends AbstractDatabaseTable {
  /**
   * Deletes a database row from the database table
   * @param value The primary key value
   * @return A future that is completed when the deletion is completed
   */
  default CompletableFuture<Void> delete(Object value) {
    return delete(DatabaseCondition.of(findPrimaryKeyColumn().name(), value));
  }

  /**
   * Deletes a database row from the database table
   * @param condition The condition with which the rows can be found
   * @return A future that is completed when the deletion is completed
   */
  default CompletableFuture<Void> delete(DatabaseCondition condition) {
    var query = new StringBuilder("DELETE FROM ");
    query.append(fullName());
    query.append(" WHERE ");
    query.append(condition.build());
    query.append(";");
    return connection().execute(query, condition.values()).thenApply(value -> null);
  }
}
