package net.taskwolf.core.database.skeleton;

import net.taskwolf.core.database.DatabaseRow;
import net.taskwolf.core.database.condition.DatabaseCondition;

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
    var query = new StringBuilder("SELECT ");
    query.append(columnNameCompilation());
    query.append(" FROM ");
    query.append(fullName());
    query.append(" WHERE ");
    query.append(condition.build());
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
