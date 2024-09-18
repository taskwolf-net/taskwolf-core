package com.dulno.core.user;

import com.dulno.core.database.*;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class UserVerificationDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "user_verification";

  public static UserVerificationDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("verificationToken", DatabaseDataType.TEXT));
    return new UserVerificationDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private UserVerificationDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertVerification(UUID id, String token) {
    insert(DatabaseRow.of(id, token));
  }

  public CompletableFuture<Boolean> verificationExists(UUID userId) {
    return exists(userId);
  }

  public void deleteVerification(UUID userId) {
    delete(userId);
  }

  public CompletableFuture<String> findVerification(UUID userId) {
    return selectRow(userId).thenApply(row ->
      row.findCell(1).stringValue());
  }
}

