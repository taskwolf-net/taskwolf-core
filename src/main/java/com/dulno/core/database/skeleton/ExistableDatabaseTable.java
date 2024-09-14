package com.dulno.core.database.skeleton;

import com.beust.jcommander.internal.Lists;
import com.dulno.core.database.DatabaseRow;
import com.dulno.core.database.condition.DatabaseComparison;
import com.dulno.core.database.condition.DatabaseCondition;

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
  default CompletableFuture<Boolean> exists(DatabaseCondition condition) {
    if (transformationState().isInactive() ||
      transformationState().isFillTemporary() || transformationState().isUseNew()
    ) {
      return existsFix(condition);
    }
    return temporaryTable().existsFix(condition);
  }

  /**
   * Is used to check whether a row inside the database table exists ignoring
   * transformation processes
   * @param row The row that will be checked for existence
   * @return A future that contains the existence boolean
   */
  default CompletableFuture<Boolean> existsFix(DatabaseRow row) {
    var primaryKeyColumns = columns().stream()
      .filter(column -> !column.type().isRegular())
      .toList();
    var comparisons = Lists.<DatabaseComparison>newArrayList();
    for (var i = 0; i < primaryKeyColumns.size(); i++) {
      comparisons.add(DatabaseComparison.create(primaryKeyColumns.get(i).name(),
        row.findCell(i).rawValue()));
    }
    return existsFix(DatabaseCondition.create(comparisons));
  }

  /**
   * Is used to check whether a row inside the database table exists ignoring
   * transformation processes
   * @param condition The condition with which the row can be found
   * @return A future that contains the existence boolean
   */
  default CompletableFuture<Boolean> existsFix(DatabaseCondition condition) {
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
