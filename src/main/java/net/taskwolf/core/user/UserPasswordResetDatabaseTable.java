package net.taskwolf.core.user;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class UserPasswordResetDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "user_password_reset";

  public static UserPasswordResetDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("resetToken", DatabaseDataType.TEXT));
    return new UserPasswordResetDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private UserPasswordResetDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertResetToken(UUID id, String token) {
    insert(DatabaseRow.of(id, token));
  }

  public CompletableFuture<Boolean> resetTokenExists(UUID userId) {
    return exists(DatabaseCell.create(userId));
  }

  public void deleteResetToken(UUID userId) {
    delete(DatabaseCell.create(userId));
  }

  public CompletableFuture<String> findResetToken(UUID userId) {
    return selectRow(DatabaseCell.create(userId)).thenApply(row ->
      row.findCell(1).stringValue());
  }
}
