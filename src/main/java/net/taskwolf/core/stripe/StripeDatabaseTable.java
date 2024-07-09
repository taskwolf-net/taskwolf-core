package net.taskwolf.core.stripe;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

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
    String accountId, UUID userId, String subscriptionId
  ) {
    return insert(DatabaseRow.of(accountId, userId, subscriptionId));
  }

  public CompletableFuture<Void> updateStripeAccount(
    String accountId, UUID userId, String subscriptionId
  ) {
    return update(DatabaseCell.create(accountId),
      DatabaseRow.of(accountId, userId, subscriptionId));
  }

  public CompletableFuture<Void> deleteStripeAccount(String accountId) {
    return delete(DatabaseCell.create(accountId));
  }

  public CompletableFuture<Boolean> stripeAccountExists(String accountId) {
    return exists(DatabaseCell.create(accountId));
  }

  public CompletableFuture<Boolean> stripeAccountExistsByUser(UUID userId) {
    return exists("user=" + userId + " ALLOW FILTERING");
  }

  public CompletableFuture<StripeAccount> findStripeAccount(String accountId) {
    return selectRow(DatabaseCell.create(accountId)).thenApply(StripeAccount::of);
  }

  public CompletableFuture<StripeAccount> findStripeAccountByUser(UUID userId) {
    return selectRow("user=" + userId + " ALLOW FILTERING")
      .thenApply(StripeAccount::of);
  }
}

