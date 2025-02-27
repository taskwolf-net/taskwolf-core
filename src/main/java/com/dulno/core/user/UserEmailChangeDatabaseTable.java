package com.dulno.core.user;

import com.dulno.core.database.*;
import com.google.common.collect.Lists;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class UserEmailChangeDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "user_email_change";

  public static UserEmailChangeDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("newEmail", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("changeToken", DatabaseDataType.TEXT));
    return new UserEmailChangeDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private UserEmailChangeDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertChange(UUID id, String newEmail, String token) {
    insert(DatabaseRow.of(id, newEmail, token), "USING TTL " + (60 * 60 * 24));
  }

  public void updateChange(UUID id, String newEmail, String token) {
    update(id, DatabaseRow.of(id, newEmail, token), "USING TTL " + (60 * 60 * 24));
  }

  public CompletableFuture<Boolean> changeExists(UUID userId) {
    return exists(userId);
  }

  public void deleteChange(UUID userId) {
    delete(userId);
  }

  public CompletableFuture<Map.Entry<String, String>> findChange(UUID userId) {
    return selectRow(userId).thenApply(row ->
      new AbstractMap.SimpleEntry<>(row.findCell(1).stringValue(),
        row.findCell(2).stringValue()));
  }
}
