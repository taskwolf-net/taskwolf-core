package net.taskwolf.core.workflow.throttle;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class WorkflowThrottleDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "workflow_throttle";

  public static WorkflowThrottleDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("target", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("executions", DatabaseDataType.COUNTER));
    columns.add(DatabaseColumn.create("expiration", DatabaseDataType.COUNTER));
    return new WorkflowThrottleDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private WorkflowThrottleDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public CompletableFuture<Void> insertThrottle(UUID targetId) {
    return updateThrottle(targetId, 0, 0);
  }

  public CompletableFuture<Void> addThrottleExecution(UUID targetId) {
    return updateThrottle(targetId, 1, 0);
  }

  public CompletableFuture<Void> setThrottle(
    UUID targetId, long executions, long expiration
  ) {
    return findThrottle(targetId).thenCompose(entry ->
      setThrottle(entry, executions, expiration));
  }

  public CompletableFuture<Void> setThrottle(
    WorkflowThrottleEntry entry, long executions, long expiration
  ) {
    return updateThrottle(entry.targetId(), executions - entry.executions(),
      expiration - entry.expiration());
  }

  private CompletableFuture<Void> updateThrottle(
    UUID targetId, long executionAddition, long expirationAddition
  ) {
    var executionQuery = new StringBuilder();
    executionQuery.append("executions");
    executionQuery.append(executionAddition >= 0 ? "+" : "-");
    executionQuery.append(Math.abs(executionAddition));
    var expirationQuery = new StringBuilder();
    expirationQuery.append("expiration");
    expirationQuery.append(expirationAddition >= 0 ? "+" : "-");
    expirationQuery.append(Math.abs(expirationAddition));
    return update(targetId, DatabaseRow.of(targetId, executionQuery,
      expirationQuery));
  }

  public CompletableFuture<Void> deleteThrottle(UUID targetId) {
    return delete(targetId);
  }

  public CompletableFuture<Boolean> throttleExists(UUID targetId) {
    return exists(targetId);
  }

  public CompletableFuture<WorkflowThrottleEntry> findThrottle(UUID targetId) {
    return selectRow(targetId).thenApply(WorkflowThrottleEntry::of);
  }
}
