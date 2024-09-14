package com.dulno.core.database.skeleton;

import com.dulno.core.database.condition.DatabaseCondition;

import java.util.concurrent.CompletableFuture;

public interface DeletableDatabaseTable extends AbstractDatabaseTable,
  TransformableDatabaseTable
{
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
    if (transformationState().isInactive() || transformationState().isUseNew()) {
      return deleteFix(condition);
    }
    if (transformationState().isUseTemporary()) {
      return temporaryTable().deleteFix(condition);
    }
    if (transformationState().isFillTemporary()) {
      temporaryTable().deleteFix(condition);
      return deleteFix(condition);
    }
    if (transformationState().isFillNew()) {
      deleteFix(condition);
      return temporaryTable().deleteFix(condition);
    }
    return CompletableFuture.completedFuture(null);
  }

  /**
   * Deletes a database row from the database table
   * @param condition The condition with which the rows can be found
   * @return A future that is completed when the deletion is completed
   */
  default CompletableFuture<Void> deleteFix(DatabaseCondition condition) {
    var query = new StringBuilder("DELETE FROM ");
    query.append(fullName());
    var conditionValue = condition.build();
    if (!conditionValue.isEmpty()) {
      query.append(" WHERE ");
      query.append(conditionValue);
    }
    query.append(";");
    return connection().execute(query, condition.values())
      .thenApply(value -> null);
  }
}
