package net.taskwolf.core.stripe;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;
import net.taskwolf.core.workflow.operation.Operation;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class StripeDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "stripe";

  public static StripeDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("userId", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("accountId", DatabaseDataType.TEXT));
    return new StripeDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private StripeDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertStripeAccount(StripeAccount account) {
    insertStripeAccount(account.userId(), account.accountId());
  }

  public void insertStripeAccount(UUID userId, String accountId) {
    insert(DatabaseRow.of(userId, accountId));
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

  public void deleteStripeAccount(UUID targetId) {
    delete(DatabaseCell.create(targetId));
  }

  public CompletableFuture<Boolean> operationsExists(UUID targetId) {
    return exists(DatabaseCell.create(targetId));
  }

  public CompletableFuture<Operation> findOperations(UUID targetId) {
    return selectRow(DatabaseCell.create(targetId)).thenApply(Operation::of);
  }
}

