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
    columns.add(DatabaseColumn.create("userId", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("accountId", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("subscriptionId", DatabaseDataType.TEXT));
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
    insert(DatabaseRow.of(userId, accountId, null));
  }

  public void updateStripeAccountSubscription(
    StripeAccount account, String subscriptionId
  ) {
    account.updateSubscription(subscriptionId);
    updateStripeAccount(account);
  }

  private void updateStripeAccount(StripeAccount account) {
    update(DatabaseCell.create(account.userId()), DatabaseRow.of(
      account.userId(), account.accountId()));
  }

  public void deleteStripeAccount(UUID userId) {
    delete(DatabaseCell.create(userId));
  }

  public CompletableFuture<Boolean> stripeAccountExists(UUID userId) {
    return exists(DatabaseCell.create(userId));
  }

  public CompletableFuture<Boolean> stripeAccountExistsById(String accountId) {
    return exists("accountId='" + accountId + "' ALLOW FILTERING");
  }

  public CompletableFuture<StripeAccount> findStripeAccount(UUID userId) {
    return selectRow(DatabaseCell.create(userId)).thenApply(StripeAccount::of);
  }

  public CompletableFuture<StripeAccount> findStripeAccountById(String accountId) {
    return selectRow("accountId='" + accountId + "' ALLOW FILTERING")
      .thenApply(StripeAccount::of);
  }
}

