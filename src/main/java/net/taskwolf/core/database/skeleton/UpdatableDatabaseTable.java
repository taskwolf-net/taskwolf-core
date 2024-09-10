package net.taskwolf.core.database.skeleton;

import net.taskwolf.core.database.DatabaseAccessType;
import net.taskwolf.core.database.DatabaseRow;
import net.taskwolf.core.database.condition.DatabaseCondition;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public interface UpdatableDatabaseTable extends AbstractDatabaseTable,
  TransformableDatabaseTable
{
  /**
   * Updates a row inside the database table
   * @param value The primary key value
   * @param row The updated row (with all the columns)
   * @return A future that is completed when the update is completed
   */
  default CompletableFuture<Void> update(Object value, DatabaseRow row) {
    return update(DatabaseCondition.of(findPrimaryKeyColumn().name(), value), row);
  }

  /**
   * Updates a row inside the database table
   * @param condition The condition with which the row can be found
   * @param row The updated row (with all the columns)
   * @return A future that is completed when the update is completed
   */
  default CompletableFuture<Void> update(
    DatabaseCondition condition, DatabaseRow row
  ) {
    return update(condition, row.values(), buildUpdateKeyValuePairs());
  }

  private String buildUpdateKeyValuePairs() {
    var pairs = new StringBuilder();
    for (var i = 0; i < columns().size(); i++) {
      var column = columns().get(i);
      if (!column.type().isRegular()) {
        continue;
      }
      pairs.append(column.name());
      pairs.append(" = ?");
      if (i < columns().size() - 1) {
        pairs.append(", ");
      }
    }
    return pairs.toString();
  }

  /**
   * Updates a row inside the database table
   * @param value The primary key value
   * @param row The updated row (with all the columns)
   * @return A future that is completed when the update is completed
   */
  default CompletableFuture<Void> updateCounter(Object value, DatabaseRow row) {
    return updateCounter(DatabaseCondition.of(findPrimaryKeyColumn().name(), value),
      row);
  }

  /**
   * Updates a row inside the database table
   * @param condition The condition with which the row can be found
   * @param row The updated row (with all the columns)
   * @return A future that is completed when the update is completed
   */
  default CompletableFuture<Void> updateCounter(
    DatabaseCondition condition, DatabaseRow row
  ) {
    var values = row.values();
    for (var i = condition.values().length; i < values.length; i++) {
      values[i] = Math.abs((long) values[i]);
    }
    return update(condition, values, buildUpdateCounterKeyValuePairs(row));
  }

  private String buildUpdateCounterKeyValuePairs(DatabaseRow row) {
    var pairs = new StringBuilder();
    for (var i = 0; i < columns().size(); i++) {
      var column = columns().get(i);
      if (!column.type().isRegular()) {
        continue;
      }
      pairs.append(column.name());
      pairs.append(" = ");
      pairs.append(column.name());
      var value = row.findCell(i).longValue();
      pairs.append(value >= 0 ? " + " : " - ");
      pairs.append("?");
      if (i < columns().size() - 1) {
        pairs.append(", ");
      }
    }
    return pairs.toString();
  }

  private CompletableFuture<Void> update(
    DatabaseCondition condition, Object[] values, String keyValuePairs
  ) {
    //TODO: TRANSFORM INPUT
    if (transformationState().isInactive() || transformationState().isUseNew()) {
      return updateFix(condition, values, keyValuePairs);
    }
    if (transformationState().isUseTemporary()) {
      return temporaryTable().updateFix(condition, values, keyValuePairs);
    }
    if (transformationState().isFillTemporary()) {
      temporaryTable().updateFix(condition, values, keyValuePairs);
      return updateFix(condition, values, keyValuePairs);
    }
    if (transformationState().isFillNew()) {
      updateFix(condition, values, keyValuePairs);
      return temporaryTable().updateFix(condition, values, keyValuePairs);
    }
    return CompletableFuture.completedFuture(null);
  }

  /**
   * Updates a row inside the database table ignoring transformation processes
   * @param condition The condition with which the row can be found
   * @param values The values used to replace the placeholders
   * @param keyValuePairs The key value pairs that are used in the update query
   * @return A future that is completed when the update is completed
   */
  default CompletableFuture<Void> updateFix(
    DatabaseCondition condition, Object[] values, String keyValuePairs
  ) {
    var query = new StringBuilder("UPDATE ");
    query.append(fullName());
    query.append(" SET ");
    query.append(keyValuePairs);
    var conditionValue = condition.build();
    if (!conditionValue.isEmpty()) {
      query.append(" WHERE ");
      query.append(conditionValue);
    }
    query.append(condition.filteringAddition());
    query.append(";");
    var conditionValues = condition.values();
    return connection().execute(query,
        Stream.concat(Arrays.stream(values), Arrays.stream(conditionValues))
          .skip(conditionValues.length).toArray(Object[]::new))
      .thenApply(value -> null);
  }
}
