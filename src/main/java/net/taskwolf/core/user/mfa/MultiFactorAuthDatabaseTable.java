package net.taskwolf.core.user.mfa;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class MultiFactorAuthDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "multi_factor_auth";

  public static MultiFactorAuthDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("user", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("secret", DatabaseDataType.TEXT));
    columns.add(DatabaseListColumn.create("recoveryCodes", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("confirmed", DatabaseDataType.BOOLEAN));
    return new MultiFactorAuthDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private MultiFactorAuthDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public CompletableFuture<Void> insertAuth(
    UUID userId, String secret, List<String> recoveryCodes
  ) {
    return insert(DatabaseRow.of(userId, secret, recoveryCodes, false),
      "USING TTL " + (60 * 60));
  }

  public CompletableFuture<Void> confirmAuth(UUID userId) {
    return findAuth(userId).thenCompose(auth ->
      deleteAuth(userId).thenCompose(value ->
        insert(DatabaseRow.of(userId, auth.secret(), auth.recoveryCodes(), true))));
  }

  public CompletableFuture<Boolean> authExists(UUID userId) {
    return exists(DatabaseCell.create(userId));
  }

  public CompletableFuture<Void> deleteAuth(UUID userId) {
    return delete(DatabaseCell.create(userId));
  }

  public CompletableFuture<MultiFactorAuthUser> findAuth(UUID userId) {
    return selectRow(DatabaseCell.create(userId))
      .thenApply(MultiFactorAuthUser::of);
  }
}
