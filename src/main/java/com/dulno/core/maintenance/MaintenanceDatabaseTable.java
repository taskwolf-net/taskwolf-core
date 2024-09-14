package com.dulno.core.maintenance;

import com.dulno.core.database.*;
import com.google.common.collect.Lists;
import com.dulno.core.database.*;
import com.dulno.core.database.condition.DatabaseCondition;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class MaintenanceDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "maintenance";

  public static MaintenanceDatabaseTable create(
          DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("description", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("startTime", DatabaseDataType.BIGINT));
    columns.add(DatabaseColumn.create("duration", DatabaseDataType.BIGINT));
    columns.add(DatabaseColumn.create("status", DatabaseDataType.TEXT));
    var maintenanceDatabaseTable =  new MaintenanceDatabaseTable(connection,
      keyspace, TABLE_NAME, columns);
    maintenanceDatabaseTable.createIfNotExists();
    maintenanceDatabaseTable.createIndexIfNotExists("status");
    return maintenanceDatabaseTable;
  }

  private MaintenanceDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public CompletableFuture<Void> insertMaintenance(Maintenance maintenance) {
    return insertMaintenance(maintenance.id(), maintenance.description(),
      maintenance.startTime(), maintenance.duration(), maintenance.status());
  }

  public CompletableFuture<Void> insertMaintenance(
    UUID id, String description, long startTime, long duration,
    MaintenanceStatus status
  ) {
    return insert(DatabaseRow.of(id, description, startTime, duration,
      status.toString()));
  }

  public CompletableFuture<Void> updateMaintenanceStatus(
    UUID id, MaintenanceStatus status
  ) {
    var futureResponse = new CompletableFuture<Void>();
    findMaintenance(id)
      .thenAccept(maintenance -> updateMaintenanceStatus(maintenance, status)
        .thenAccept(futureResponse::complete));
    return futureResponse;
  }

  public CompletableFuture<Void> updateMaintenanceStatus(
    Maintenance maintenance, MaintenanceStatus status
  ) {
    maintenance.updateStatus(status);
    return updateMaintenance(maintenance);
  }

  private CompletableFuture<Void> updateMaintenance(Maintenance maintenance) {
    return update(maintenance.id(),
      DatabaseRow.of(maintenance.id(), maintenance.description(),
        maintenance.startTime(), maintenance.duration(),
        maintenance.status().toString()));
  }

  public CompletableFuture<UUID> generateAvailableMaintenanceId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    maintenanceExists(id).thenApply(exists -> exists ?
      generateAvailableMaintenanceId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Void> deleteMaintenance(UUID id) {
    return delete(id);
  }

  public CompletableFuture<Boolean> maintenanceExists(UUID id) {
    return exists(id);
  }

  public CompletableFuture<Maintenance> findMaintenance(UUID id) {
    return selectRow(id).thenApply(Maintenance::of);
  }

  public CompletableFuture<List<Maintenance>> findMaintenanceByStatus(
    MaintenanceStatus status
  ) {
    return selectRows(DatabaseCondition.of("status", status.toString()))
      .thenApply(rows -> rows.stream().map(Maintenance::of).toList());
  }

  public CompletableFuture<List<Maintenance>> findAllMaintenance() {
    return selectAllRows().thenApply(rows ->
      rows.stream().map(Maintenance::of).collect(Collectors.toList()));
  }
}