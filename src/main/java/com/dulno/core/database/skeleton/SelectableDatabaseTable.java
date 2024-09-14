package com.dulno.core.database.skeleton;

import com.dulno.core.database.DatabaseRow;
import com.dulno.core.iterator.AsyncIterator;
import com.dulno.core.database.condition.DatabaseCondition;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface SelectableDatabaseTable extends AbstractDatabaseTable,
  TransformableDatabaseTable
{
  /**
   * Finds all available rows inside the database table
   * @return List of all possible rows
   */
  default CompletableFuture<List<DatabaseRow>> selectAllRows() {
    CompletableFuture<List<DatabaseRow>> result;
    if (transformationState().isInactive() ||
      transformationState().isFillTemporary() || transformationState().isUseNew()
    ) {
      result = selectAllRowsFix();
    } else {
      result = temporaryTable().selectAllRowsFix();
    }
    if (transformation() != null && (transformationState().isFillTemporary() ||
      transformationState().isUseTemporary() || transformationState().isFillNew())
    ) {
      return result.thenCompose(rows -> AsyncIterator.execute(rows,
        row -> transformation().transformOldToNew(row)));
    }
    return result;
  }

  /**
   * Finds all available rows inside the database table ignoring
   * transformation processes
   * @return List of all possible rows
   */
  default CompletableFuture<List<DatabaseRow>> selectAllRowsFix() {
    var query = new StringBuilder("SELECT ");
    query.append(columnNameCompilation());
    query.append(" FROM ");
    query.append(fullName());
    query.append(";");
    return connection().execute(query).thenApply(result ->
      DatabaseRow.multiple(result.currentPage(), columns().size()));
  }

  /**
   * Is used to find a single row
   * @param value The primary key value
   * @return A future that contains the database row
   */
  default CompletableFuture<DatabaseRow> selectRow(Object value) {
    return selectRow(DatabaseCondition.of(findPrimaryKeyColumn().name(), value));
  }

  /**
   * Is used to find a single row
   * @param condition The condition with which the row can be found
   * @return A future that contains the database row
   */
  default CompletableFuture<DatabaseRow> selectRow(
    DatabaseCondition condition
  ) {
    var futureResponse = new CompletableFuture<DatabaseRow>();
    selectRows(condition).thenAccept(rows -> futureResponse.complete(rows.get(0)));
    return futureResponse;
  }

  /**
   * Is used to find a single row secured (optional result)
   * @param value The primary key value
   * @return A future that contains the database row
   */
  default CompletableFuture<Optional<DatabaseRow>> selectRowSecure(Object value) {
    return selectRowSecure(DatabaseCondition.of(findPrimaryKeyColumn().name(), value));
  }

  /**
   * Is used to find a single row secured (optional result)
   * @param condition The condition with which the row can be found
   * @return A future that contains the database row
   */
  default CompletableFuture<Optional<DatabaseRow>> selectRowSecure(
    DatabaseCondition condition
  ) {
    var futureResponse = new CompletableFuture<Optional<DatabaseRow>>();
    selectRows(condition).thenAccept(rows ->
      futureResponse.complete(rows.stream().findFirst()));
    return futureResponse;
  }

  /**
   * Is used to find a multiple rows
   * @param condition The condition with which the rows can be found
   * @return A future that contains the database rows
   */
  default CompletableFuture<List<DatabaseRow>> selectRows(
    DatabaseCondition condition
  ) {
    return selectRows(condition, -1);
  }

  /**
   * Is used to find a multiple rows
   * @param condition The condition with which the rows can be found
   * @param limit The limit of entries that should be returned
   * @return A future that contains the database rows
   */
  default CompletableFuture<List<DatabaseRow>> selectRows(
    DatabaseCondition condition, long limit
  ) {
    CompletableFuture<List<DatabaseRow>> result;
    if (transformationState().isInactive() ||
      transformationState().isFillTemporary() || transformationState().isUseNew()
    ) {
      result = selectRowsFix(condition, limit);
    } else {
      result = temporaryTable().selectRowsFix(condition, limit);
    }
    if (transformation() != null && (transformationState().isFillTemporary() ||
      transformationState().isUseTemporary() || transformationState().isFillNew())
    ) {
      return result.thenCompose(rows -> AsyncIterator.execute(rows,
        row -> transformation().transformOldToNew(row)));
    }
    return result;
  }

  /**
   * Is used to find a multiple rows ignoring transformation processes
   * @param condition The condition with which the rows can be found
   * @param limit The limit of entries that should be returned
   * @return A future that contains the database rows
   */
  default CompletableFuture<List<DatabaseRow>> selectRowsFix(
    DatabaseCondition condition, long limit
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
    if (limit > 0) {
      query.append(" LIMIT ");
      query.append(limit);
    }
    query.append(condition.filteringAddition());
    query.append(";");
    return connection().execute(query, condition.values()).thenApply(result ->
      DatabaseRow.multiple(result.currentPage(), columns().size()));
  }
}
