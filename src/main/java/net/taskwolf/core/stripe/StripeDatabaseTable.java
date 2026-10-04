package net.taskwolf.core.stripe;

import net.taskwolf.core.database.*;
import com.google.common.collect.Lists;
import net.taskwolf.core.database.condition.DatabaseCondition;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class StripeDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "stripe";

  public static StripeDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("account", DatabaseDataType.TEXT,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("target", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("user", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("subscription", DatabaseDataType.TEXT));
    return new StripeDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private StripeDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public CompletableFuture<Void> insertStripeAccount(
    String accountId, UUID targetId, UUID userId, String subscriptionId
  ) {
    return insert(DatabaseRow.of(accountId, targetId, userId, subscriptionId));
  }

  public CompletableFuture<Void> updateStripeAccount(
    String accountId, UUID targetId, UUID userId, String subscriptionId
  ) {
    return update(accountId, DatabaseRow.of(accountId, targetId, userId,
      subscriptionId));
  }

  public CompletableFuture<Void> deleteStripeAccount(String accountId) {
    return delete(accountId);
  }

  public CompletableFuture<Boolean> stripeAccountExists(String accountId) {
    return exists(accountId);
  }

  public CompletableFuture<Boolean> stripeAccountExistsByTarget(UUID targetId) {
    return exists(DatabaseCondition.of("target", targetId));
  }

  public CompletableFuture<StripeAccount> findStripeAccount(String accountId) {
    return selectRow(accountId).thenApply(StripeAccount::of);
  }

  public CompletableFuture<StripeAccount> findStripeAccountByTarget(UUID targetId) {
    return selectRow(DatabaseCondition.of("target", targetId))
      .thenApply(StripeAccount::of);
  }
}

