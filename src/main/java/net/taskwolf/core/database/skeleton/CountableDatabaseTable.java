package net.taskwolf.core.database.skeleton;

import net.taskwolf.core.database.condition.DatabaseCondition;

import java.util.concurrent.CompletableFuture;

public interface CountableDatabaseTable extends AbstractDatabaseTable,
  TransformableDatabaseTable
{
  /**
   * Is used to find the number of rows inside a database table
   * @return The number of rows
   */
  default CompletableFuture<Long> count() {
    var query = new StringBuilder("SELECT COUNT(*) FROM ");
    query.append(fullName());
    query.append(";");
    return connection().execute(query)
      .thenApply(result -> result.one().get(0, Long.class));
  }

  /**
   * Is used to find the number of rows inside a database table
   * @param condition The condition for counting
   * @return The number of rows
   */
  default CompletableFuture<Long> count(
    DatabaseCondition condition
  ) {
    var query = new StringBuilder("SELECT COUNT(*) FROM ");
    query.append(fullName());
    var conditionValue = condition.build();
    if (!conditionValue.isEmpty()) {
      query.append(" WHERE ");
      query.append(conditionValue);
    }
    query.append(condition.filteringAddition());
    query.append(";");
    return connection().execute(query, condition.values())
      .thenApply(result -> result.one().get(0, Long.class));
  }
}
