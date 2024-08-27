package net.taskwolf.core.session;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class SessionDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "session";

  public static SessionDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PARTITION_KEY));
    columns.add(DatabaseColumn.create("user", DatabaseDataType.UUID,
      DatabaseColumn.Type.CLUSTERING_KEY));
    columns.add(DatabaseColumn.create("devicePlatform", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("ipAddress", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("country", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("city", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("openTime", DatabaseDataType.BIGINT));
    columns.add(DatabaseColumn.create("refreshToken", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("status", DatabaseDataType.TEXT));
    var table = new SessionDatabaseTable(connection, keyspace, TABLE_NAME, columns);
    table.createIfNotExists();
    table.createIndexIfNotExists("id");
    table.createIndexIfNotExists("user");
    table.createIndexIfNotExists("status");
    table.initializeViews();
    return table;
  }

  private DatabaseTable openTimeView;

  private SessionDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  private void initializeViews() {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("user", DatabaseDataType.UUID,
      DatabaseColumn.Type.PARTITION_KEY));
    columns.add(DatabaseColumn.create("openTime", DatabaseDataType.BIGINT,
      DatabaseColumn.Type.CLUSTERING_KEY));
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.CLUSTERING_KEY));
    columns.add(DatabaseColumn.create("devicePlatform", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("ipAddress", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("country", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("city", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("refreshToken", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("status", DatabaseDataType.TEXT));
    openTimeView = createMaterializedViewIfNotExists("open_time_view", columns);
  }

  public CompletableFuture<Void> insertSession(Session session) {
    return insertSession(session.id(), session.userId(), session.devicePlatform(),
      session.ipAddress(), session.country(), session.city(), session.openTime(),
      session.lastRefreshToken(), session.status());
  }

  public CompletableFuture<Void> insertSession(
    UUID id, UUID userId, String devicePlatform, String ipAddress, String country,
    String city, long openTime, String refreshToken, SessionStatus status
  ) {
    return insert(DatabaseRow.of(id, userId, devicePlatform, ipAddress, country,
      city, openTime, refreshToken, status.toString()));
  }

  public CompletableFuture<Void> updateSessionRefreshToken(
    UUID id, String refreshToken
  ) {
    var futureResponse = new CompletableFuture<Void>();
    findSession(id)
      .thenAccept(session -> updateSessionRefreshToken(session, refreshToken)
      .thenAccept(futureResponse::complete));
    return futureResponse;
  }

  public CompletableFuture<Void> updateSessionRefreshToken(
    Session session, String refreshToken
  ) {
    session.updateRefreshToken(refreshToken);
    return updateSession(session);
  }

  public CompletableFuture<Void> closeSession(UUID id) {
    var futureResponse = new CompletableFuture<Void>();
    findSession(id).thenAccept(session -> closeSession(session)
      .thenAccept(futureResponse::complete));
    return futureResponse;
  }

  public CompletableFuture<Void> closeSession(Session session) {
    session.close();
    return updateSession(session);
  }

  public CompletableFuture<Void> updateSession(Session session) {
    return update(DatabaseCell.create(session.id()),
      DatabaseRow.of(session.id(), session.userId(), session.devicePlatform(),
        session.ipAddress(), session.country(), session.city(), session.openTime(),
        session.lastRefreshToken(), session.status().toString()));
  }

  public CompletableFuture<Void> deleteSession(UUID id) {
    return delete(DatabaseCell.create(id));
  }

  public CompletableFuture<UUID> generateAvailableSessionId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    sessionExists(id).thenApply(exists -> exists ?
      generateAvailableSessionId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Boolean> sessionExists(UUID id) {
    return exists(DatabaseCell.create(id));
  }

  public CompletableFuture<Session> findSession(UUID id) {
    return selectRow(DatabaseCell.create(id)).thenApply(row -> Session.of(row, this));
  }

  public CompletableFuture<List<Session>> findSessionsOfUser(UUID userId) {
    return selectRows("user=" + userId)
      .thenApply(rows -> rows.stream().map(row -> Session.of(row, this)).toList());
  }

  public CompletableFuture<List<Session>> findSessionsOfUserByStatus(
    UUID userId, SessionStatus status
  ) {
    return selectRows("user=" + userId + " AND status='" + status.toString() +
      "' ALLOW FILTERING")
      .thenApply(rows -> rows.stream().map(row -> Session.of(row, this)).toList());
  }

  private static final int MAX_LAST_SESSIONS = 5;

  public CompletableFuture<List<Session>> findLastSessionsOfUser(UUID userId) {
    return openTimeView.selectRows("user=" + userId + " ORDER BY openTime" +
      " LIMIT " + MAX_LAST_SESSIONS)
      .thenApply(rows -> rows.stream().map(row -> Session.of(row, openTimeView))
        .toList());
  }
}