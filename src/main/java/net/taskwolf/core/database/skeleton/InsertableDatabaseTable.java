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
    if (transformationState().isInactive() || transformationState().isUseNew()) {
      return insertFix(row, addition);
    }
    if (transformationState().isFillTemporary()) {
      var transformation = transformation().transformNewToOld(row);
      transformation.thenAccept(transformedRow ->
        temporaryTable().insertFix(transformedRow, addition));
      return transformation.thenCompose(transformedRow ->
        insertFix(transformedRow, addition));
    }
    if (transformationState().isUseTemporary()) {
      return transformation().transformNewToOld(row).thenCompose(transformedRow ->
        temporaryTable().insertFix(transformedRow, addition));
    }
    if (transformationState().isFillNew()) {
      insertFix(row, addition);
      return transformation().transformNewToOld(row).thenCompose(transformedRow ->
        temporaryTable().insertFix(transformedRow, addition));
    }
    return CompletableFuture.completedFuture(null);
  }

  /**
   * Inserts a new database row into the database table ignoring
   * transformation processes
   * @param row The database row that is to be inserted
   * @return A future that is completed when the insertion is completed
   */
  default CompletableFuture<Void> insertFix(DatabaseRow row) {
    return insertFix(row, "");
  }

  /**
   * Inserts a new database row into the database table ignoring
   * transformation processes
   * @param row The database row that is to be inserted
   * @param addition An addition insertion argument (for example for ttl)
   * @return A future that is completed when the insertion is completed
   */
  default CompletableFuture<Void> insertFix(DatabaseRow row, String addition) {
    var query = new StringBuilder("INSERT INTO ");
    query.append(fullName());
    query.append(" (");
    query.append(columnNameCompilation());
    query.append(") VALUES (");
    query.append(row.placeholderCompilation());
    query.append(") ");
    query.append(addition);
    query.append(";");
    return connection().execute(query, row.values())
      .thenApply(value -> null);
  }
}
