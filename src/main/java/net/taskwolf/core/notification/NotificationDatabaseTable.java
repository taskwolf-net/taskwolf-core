package net.taskwolf.core.notification;

import net.taskwolf.core.database.*;
import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class NotificationDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "notification";

  public static NotificationDatabaseTable create(
          DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("general", DatabaseDataType.BOOLEAN));
    columns.add(DatabaseColumn.create("workflowFail", DatabaseDataType.BOOLEAN));
    return new NotificationDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private NotificationDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertNotificationSettings(NotificationSetting setting) {
    insertNotificationSettings(setting.userId(), setting.general(),
      setting.workflowFail());
  }

  public void insertNotificationSettings(
    UUID userId, boolean general, boolean workflowFail
  ) {
    insert(DatabaseRow.of(userId, general, workflowFail));
  }

  public void changeNotificationSettings(
    UUID userId, boolean general, boolean workflowFail
  ) {
    update(userId, DatabaseRow.of(userId, general, workflowFail));
  }

  public CompletableFuture<Boolean> notificationSettingsExists(UUID userId) {
    return exists(userId);
  }

  public void deleteNotificationSettings(UUID userId) {
    delete(userId);
  }

  public CompletableFuture<NotificationSetting> findNotificationSettings(
    UUID userId
  ) {
    return selectRow(userId).thenApply(NotificationSetting::of);
  }
}
