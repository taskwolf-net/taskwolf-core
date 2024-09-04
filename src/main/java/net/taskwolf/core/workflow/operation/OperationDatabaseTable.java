package net.taskwolf.core.workflow.operation;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;
import net.taskwolf.core.workflow.throttle.WorkflowThrottleEntry;

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
    columns.add(DatabaseColumn.create("operations", DatabaseDataType.COUNTER));
    columns.add(DatabaseColumn.create("expiration", DatabaseDataType.COUNTER));
    return new OperationDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private OperationDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public CompletableFuture<Void> insertOperations(UUID targetId) {
    return updateOperations(targetId, 0, System.currentTimeMillis() +
      1000L * 60 * 60 * 24 * 30);
  }

  public CompletableFuture<Void> addOperations(
    UUID targetId, long additionalOperations
  ) {
    return updateOperations(targetId, additionalOperations, 0);
  }

  public CompletableFuture<Void> extendExpiration(UUID targetId) {
    return findOperations(targetId).thenAccept(this::extendExpiration);
  }

  private CompletableFuture<Void> extendExpiration(Operation operation) {
    return setOperations(operation, 0, operation.expiration() +
      1000L * 60 * 60 * 24 * 30);
  }

  public CompletableFuture<Void> resetExpiration(UUID targetId) {
    return findOperations(targetId).thenAccept(this::resetExpiration);
  }

  private CompletableFuture<Void> resetExpiration(Operation operation) {
    return setOperations(operation, 0, System.currentTimeMillis() +
      1000L * 60 * 60 * 24 * 30);
  }

  private CompletableFuture<Void> setOperations(
    Operation entry, long operations, long expiration
  ) {
    return updateOperations(entry.targetId(), operations - entry.operations(),
      expiration - entry.expiration());
  }

  private CompletableFuture<Void> updateOperations(
    UUID targetId, long operationAddition, long expirationAddition
  ) {
    var operationQuery = new StringBuilder();
    operationQuery.append("operations");
    operationQuery.append(operationAddition >= 0 ? "+" : "-");
    operationQuery.append(Math.abs(operationAddition));
    var expirationQuery = new StringBuilder();
    expirationQuery.append("expiration");
    expirationQuery.append(expirationAddition >= 0 ? "+" : "-");
    expirationQuery.append(Math.abs(expirationAddition));
    return update(targetId, DatabaseRow.of(targetId, operationQuery,
      expirationQuery));
  }

  public CompletableFuture<Void> deleteOperations(UUID targetId) {
    return delete(targetId);
  }

  public CompletableFuture<Boolean> operationsExists(UUID targetId) {
    return exists(targetId);
  }

  public CompletableFuture<Operation> findOperations(UUID targetId) {
    return selectRow(targetId).thenApply(Operation::of);
  }
}
