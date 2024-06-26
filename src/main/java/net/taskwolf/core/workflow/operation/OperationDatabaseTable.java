package net.taskwolf.core.workflow.operation;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class OperationDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "operation";

  public static OperationDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("target", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("operations", DatabaseDataType.BIGINT));
    columns.add(DatabaseColumn.create("expiration", DatabaseDataType.BIGINT));
    return new OperationDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private OperationDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertOperations(UUID targetId) {
    insert(DatabaseRow.of(targetId, 0, System.currentTimeMillis() +
      1000L * 60 * 60 * 24 * 30));
  }

  public void addOperations(UUID targetId, long additionalOperations) {
    findOperations(targetId).thenAccept(operation -> addOperations(operation,
      additionalOperations));
  }

  public void addOperations(Operation operation, long additionalOperations) {
    operation.addOperations(additionalOperations);
    updateOperations(operation);
  }

  public void extendExpiration(UUID targetId) {
    findOperations(targetId).thenAccept(this::resetExpiration);
  }

  public void extendExpiration(Operation operation) {
    operation.resetExpiration();
    updateOperations(operation);
  }

  public void resetExpiration(UUID targetId) {
    findOperations(targetId).thenAccept(this::resetExpiration);
  }

  public void resetExpiration(Operation operation) {
    operation.resetExpiration();
    updateOperations(operation);
  }

  private void updateOperations(Operation operation) {
    update(DatabaseCell.create(operation.targetId()), DatabaseRow.of(
      operation.targetId(), operation.operations(), operation.expiration()));
  }

  public void deleteOperations(UUID targetId) {
    delete(DatabaseCell.create(targetId));
  }

  public CompletableFuture<Boolean> operationsExists(UUID targetId) {
    return exists(DatabaseCell.create(targetId));
  }

  public CompletableFuture<Operation> findOperations(UUID targetId) {
    return selectRow(DatabaseCell.create(targetId)).thenApply(Operation::of);
  }
}
