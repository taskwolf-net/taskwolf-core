package net.taskwolf.core.database.skeleton;

import net.taskwolf.core.database.DatabaseRow;
import net.taskwolf.core.database.condition.DatabaseCondition;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public interface UpdatableDatabaseTable extends AbstractDatabaseTable {
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
    var query = new StringBuilder("UPDATE ");
    query.append(fullName());
    query.append(" SET ");
    query.append(buildUpdateKeyValuePairs());
    query.append(" WHERE ");
    query.append(condition.build());
    query.append(condition.filteringAddition());
    query.append(";");
    return connection().execute(query, Stream.concat(Arrays.stream(row.values()),
        Arrays.stream(condition.values())).skip(1).toArray(Object[]::new))
      .thenApply(value -> null);
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
}
