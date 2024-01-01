package net.taskwolf.core.user;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class ProfilePictureDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "profile_picture";

  public static ProfilePictureDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("picture", DatabaseDataType.TEXT));
    return new ProfilePictureDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private ProfilePictureDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertProfilePicture(UUID userId, String picture) {
    insert(DatabaseRow.of(userId, picture));
  }

  public void changeProfilePicture(UUID userId, String newPicture) {
    update(DatabaseCell.create(userId), DatabaseRow.of(userId, newPicture));
  }

  public CompletableFuture<Boolean> profilePictureExists(UUID userId) {
    return exists(DatabaseCell.create(userId));
  }

  public void deleteProfilePicture(UUID userId) {
    delete(DatabaseCell.create(userId));
  }

  public CompletableFuture<String> findProfilePicture(UUID userId) {
    return selectRow(DatabaseCell.create(userId)).thenApply(row ->
      row.findCell(1).stringValue());
  }
}
