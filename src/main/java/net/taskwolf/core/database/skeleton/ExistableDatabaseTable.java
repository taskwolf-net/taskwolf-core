package net.taskwolf.core.database.skeleton;

import net.taskwolf.core.database.condition.DatabaseCondition;

import java.util.concurrent.CompletableFuture;

public interface ExistableDatabaseTable extends AbstractDatabaseTable,
  TransformableDatabaseTable
{
  /**
   * Is used to check whether a row inside the database table exists
   * @param value The primary key value
   * @return A future that contains the existence boolean
   */
  default CompletableFuture<Boolean> exists(Object value) {
    if (value == null) {
      return CompletableFuture.completedFuture(false);
    }
    return exists(DatabaseCondition.of(findPrimaryKeyColumn().name(), value));
  }

  /**
   * Is used to check whether a row inside the database table exists
   * @param condition The condition with which the row can be found
   * @return A future that contains the existence boolean
   */
  default CompletableFuture<Boolean> exists(
    DatabaseCondition condition
  ) {
    var query = new StringBuilder("SELECT ");
    query.append(columnNameCompilation());
    query.append(" FROM ");
    query.append(fullName());
    var conditionValue = condition.build();
    if (!conditionValue.isEmpty()) {
      query.append(" WHERE ");
      query.append(conditionValue);
    }
    query.append(condition.filteringAddition());
    query.append(";");
    return connection().execute(query, condition.values())
      .thenApply(result -> result.remaining() > 0);
  }
}
