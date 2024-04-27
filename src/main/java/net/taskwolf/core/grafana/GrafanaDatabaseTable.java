package net.taskwolf.core.grafana;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class GrafanaDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "grafana";

  public static GrafanaDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("username", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("password", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("userId", DatabaseDataType.INT));
    columns.add(DatabaseColumn.create("organizationId", DatabaseDataType.INT));
    columns.add(DatabaseColumn.create("datasourceUid", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("datasourceVersion", DatabaseDataType.INT));
    columns.add(DatabaseColumn.create("dashboardUrl", DatabaseDataType.TEXT));
    return new GrafanaDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private GrafanaDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertAccount(GrafanaAccount account) {
    insertAccount(account.ownerId(), account.username(), account.password(),
      account.userId(), account.organizationId(), account.datasourceUid(),
      account.datasourceVersion(), account.dashboardUrl());
  }

  public void insertAccount(
    UUID ownerId, String username, String password, int userId,
    int organizationId, String datasourceUid, int datasourceVersion,
    String dashboardUrl
  ) {
    insert(DatabaseRow.of(ownerId, username, password, userId, organizationId,
      datasourceUid, datasourceVersion, dashboardUrl));
  }

  public void updateDatasourceVersion(
    GrafanaAccount account, int datasourceVersion
  ) {
    account.updateDatasourceVersion(datasourceVersion);
    updateAccount(account);
  }

  private void updateAccount(GrafanaAccount account) {
    update(DatabaseCell.create(account.ownerId()), DatabaseRow.of(
      account.ownerId(), account.username(), account.password(), account.userId(),
      account.organizationId(), account.datasourceUid(),
      account.datasourceVersion(), account.dashboardUrl()));
  }

  public void deleteAccount(UUID ownerId) {
    delete(DatabaseCell.create(ownerId));
  }

  public CompletableFuture<Boolean> accountExists(UUID ownerId) {
    return exists(DatabaseCell.create(ownerId));
  }

  public CompletableFuture<GrafanaAccount> findAccount(UUID ownerId) {
    return selectRow(DatabaseCell.create(ownerId)).thenApply(GrafanaAccount::of);
  }
}
