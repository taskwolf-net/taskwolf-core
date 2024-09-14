package com.dulno.core.user.activity;

import com.dulno.core.database.*;
import com.google.common.collect.Lists;
import com.dulno.core.database.*;
import com.dulno.core.database.condition.DatabaseCondition;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class UserActivityDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "user_activity";

  public static UserActivityDatabaseTable create(
          DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("user", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("title", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("description", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("time", DatabaseDataType.BIGINT));
    columns.add(DatabaseColumn.create("type", DatabaseDataType.TEXT));
    return new UserActivityDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private UserActivityDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertActivity(Activity activity) {
    insertActivity(activity.id(), activity.userId(), activity.title(),
      activity.description(), activity.time(), activity.type());
  }

  public void insertActivity(
    UUID id, UUID userId, String title, String description, long time,
    ActivityType type
  ) {
    insert(DatabaseRow.of(id, userId, title, description, time, type.toString()));
  }

  public void insertActivity(
    UUID userId, String title, String description, ActivityType type
  ) {
    generateAvailableActivityId().thenAccept(id -> insert(DatabaseRow.of(id,
      userId, title, description, System.currentTimeMillis(), type.toString())));
  }

  public CompletableFuture<UUID> generateAvailableActivityId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    activityExists(id).thenApply(exists -> exists ?
      generateAvailableActivityId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Boolean> activityExists(UUID id) {
    return exists(id);
  }

  public void deleteActivity(UUID id) {
    delete(id);
  }

  public CompletableFuture<Activity> findActivity(UUID id) {
    return selectRow(id).thenApply(Activity::of);
  }

  public CompletableFuture<List<Activity>> findActivitiesOfUser(UUID userId) {
    return selectRows(DatabaseCondition.of("user", userId)).thenApply(rows ->
      rows.stream().map(Activity::of).toList());
  }
}
