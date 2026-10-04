package net.taskwolf.core.stripe;

import net.taskwolf.core.database.*;
import net.taskwolf.core.database.condition.DatabaseCondition;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class StripeCompletionDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "stripe_completion";

  public static StripeCompletionDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("user", DatabaseDataType.UUID,
      DatabaseColumn.Type.PARTITION_KEY));
    columns.add(DatabaseColumn.create("completionToken", DatabaseDataType.TEXT,
      DatabaseColumn.Type.CLUSTERING_KEY));
    columns.add(DatabaseColumn.create("confirmed", DatabaseDataType.BOOLEAN));
    return new StripeCompletionDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private StripeCompletionDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertStripeCompletion(
    UUID userId, String completionToken
  ) {
    insert(DatabaseRow.of(userId, completionToken, false),
      "USING TTL " + (60 * 60));
  }

  public CompletableFuture<Void> confirmStripeCompletion(
    UUID userId, String completionToken
  ) {
    var condition = DatabaseCondition.of("user", userId,
      "completionToken", completionToken);
    return update(condition, DatabaseRow.of(userId, completionToken, true),
      "USING TTL " + (60 * 60));
  }

  public void deleteStripeCompletion(UUID userId, String completionToken) {
    delete(DatabaseCondition.of("user", userId,
      "completionToken", completionToken));
  }

  public CompletableFuture<Boolean> stripeCompletionExists(
    UUID userId, String completionToken
  ) {
    return exists(DatabaseCondition.of("user", userId,
      "completionToken", completionToken));
  }

  public CompletableFuture<Boolean> isStripeCompletionConfirmed(
    UUID userId, String completionToken
  ) {
    return selectRow(DatabaseCondition.of("user", userId,
      "completionToken", completionToken))
      .thenApply(row -> row.findCell(2).booleanValue());
  }
}

