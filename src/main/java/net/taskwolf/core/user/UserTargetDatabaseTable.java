package net.taskwolf.core.user;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;
import net.taskwolf.core.organization.Organization;
import net.taskwolf.core.organization.OrganizationDatabaseTable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class UserTargetDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "user_target";

  public static UserTargetDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace,
    OrganizationDatabaseTable organizationDatabaseTable
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("target", DatabaseDataType.UUID));
    return new UserTargetDatabaseTable(connection, keyspace, TABLE_NAME,
      columns, organizationDatabaseTable);
  }

  private final OrganizationDatabaseTable organizationDatabaseTable;

  private UserTargetDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns,
    OrganizationDatabaseTable organizationDatabaseTable
  ) {
    super(connection, keyspace, name, columns);
    this.organizationDatabaseTable = organizationDatabaseTable;
  }

  public void insertTarget(UUID id, UUID target) {
    insert(DatabaseRow.of(id, target));
  }

  public void changeTarget(UUID id, UUID target) {
    update(DatabaseCell.create(id), DatabaseRow.of(id, target));
  }

  public CompletableFuture<Boolean> targetExists(UUID userId) {
    return exists(DatabaseCell.create(userId));
  }

  public void deleteTarget(UUID userId) {
    delete(DatabaseCell.create(userId));
  }

  public CompletableFuture<UUID> findTargetSecured(UUID userId) {
    var futureResponse = new CompletableFuture<UUID>();
    findTarget(userId).thenAccept(target -> checkTargetValidity(userId, target)
      .thenAccept(futureResponse::complete));
    return futureResponse;
  }

  private CompletableFuture<UUID> checkTargetValidity(UUID userId, UUID target) {
    if (userId.equals(target)) {
      return CompletableFuture.completedFuture(target);
    }
    var futureResponse = new CompletableFuture<UUID>();
    organizationDatabaseTable.organizationExists(target).thenAccept(exists ->
      checkTargetOrganizationExistence(userId, target, exists)
        .thenAccept(futureResponse::complete));
    return futureResponse;
  }

  private CompletableFuture<UUID> checkTargetOrganizationExistence(
    UUID userId, UUID target, boolean organizationExists
  ) {
    if (!organizationExists) {
      changeTarget(userId, userId);
      return CompletableFuture.completedFuture(userId);
    }
    return organizationDatabaseTable.findOrganization(target).thenApply(
      organization -> checkTargetOrganizationPermission(userId, organization));
  }

  private UUID checkTargetOrganizationPermission(
    UUID userId, Organization organization
  ) {
    if (!organization.owner().equals(userId) &&
      !organization.members().contains(userId)
    ) {
      changeTarget(userId, userId);
      return userId;
    }
    return organization.id();
  }

  public CompletableFuture<UUID> findTarget(UUID userId) {
    return selectRow(DatabaseCell.create(userId)).thenApply(row ->
      row.findCell(1).uuidValue());
  }
}

