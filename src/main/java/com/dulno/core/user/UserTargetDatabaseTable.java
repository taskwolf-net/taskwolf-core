package com.dulno.core.user;

import com.dulno.core.bundle.BundleDatabaseTable;
import com.dulno.core.database.*;
import com.dulno.core.iterator.AsyncIterator;
import com.google.common.collect.Lists;
import com.dulno.core.organization.OrganizationDatabaseTable;

import java.util.AbstractMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class UserTargetDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "user_target";

  public static UserTargetDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace,
    UserDatabaseTable userDatabaseTable,
    OrganizationDatabaseTable organizationDatabaseTable,
    BundleDatabaseTable bundleDatabaseTable
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("target", DatabaseDataType.UUID));
    return new UserTargetDatabaseTable(connection, keyspace, TABLE_NAME,
      columns, userDatabaseTable, organizationDatabaseTable, bundleDatabaseTable);
  }

  private final UserDatabaseTable userDatabaseTable;
  private final OrganizationDatabaseTable organizationDatabaseTable;
  private final BundleDatabaseTable bundleDatabaseTable;

  private UserTargetDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns, UserDatabaseTable userDatabaseTable,
    OrganizationDatabaseTable organizationDatabaseTable,
    BundleDatabaseTable bundleDatabaseTable
  ) {
    super(connection, keyspace, name, columns);
    this.userDatabaseTable = userDatabaseTable;
    this.organizationDatabaseTable = organizationDatabaseTable;
    this.bundleDatabaseTable = bundleDatabaseTable;
  }

  public CompletableFuture<Void> insertTarget(UUID id, UUID target) {
    return insert(DatabaseRow.of(id, target));
  }

  public CompletableFuture<Void> changeTarget(UUID id, UUID target) {
    return update(id, DatabaseRow.of(id, target));
  }

  public CompletableFuture<Boolean> targetExists(UUID userId) {
    return exists(userId);
  }

  public CompletableFuture<Void> deleteTarget(UUID userId) {
    return delete(userId);
  }

  public CompletableFuture<UUID> findTargetSecured(UUID userId) {
    return targetExists(userId).thenCompose(exists -> exists ?
      findTarget(userId).thenCompose(currentTarget ->
        checkTargetValidity(userId, currentTarget).thenCompose(valid ->
          valid ? CompletableFuture.completedFuture(currentTarget) :
            correctCurrentTarget(userId))) :
      correctCurrentTarget(userId));
  }

  private CompletableFuture<Boolean> checkTargetValidity(UUID userId, UUID target) {
    return checkTargetUsability(target)
      .thenCompose(usable -> checkTargetValidity(userId, target, usable));
  }

  private CompletableFuture<Boolean> checkTargetValidity(
    UUID userId, UUID target, boolean isUsable
  ) {
    if (!isUsable) {
      return CompletableFuture.completedFuture(false);
    }
    if (userId.equals(target)) {
      return CompletableFuture.completedFuture(true);
    }
    return organizationDatabaseTable.organizationExists(target).thenCompose(
      exists -> checkTargetOrganizationExistence(userId, target, exists));
  }

  private CompletableFuture<Boolean> checkTargetOrganizationExistence(
    UUID userId, UUID target, boolean organizationExists
  ) {
    if (!organizationExists) {
      return CompletableFuture.completedFuture(false);
    }
    return organizationDatabaseTable.findOrganization(target).thenApply(
      organization -> organization.owner().equals(userId) ||
        organization.members().contains(userId));
  }

  private CompletableFuture<UUID> correctCurrentTarget(UUID userId) {
    return userDatabaseTable.findUser(userId)
      .thenCompose(user -> filterUsableTargets(user)
        .thenCompose(organizations -> checkTargetUsability(user.id())
          .thenApply(personalBundleUsable -> correctCurrentTarget(user.id(),
            organizations, personalBundleUsable))));
  }

  private CompletableFuture<List<UUID>> filterUsableTargets(User user) {
    return AsyncIterator.execute(user.organizations(),
        organization -> checkTargetUsability(organization)
          .thenApply(usable -> new AbstractMap.SimpleEntry<>(organization, usable)))
      .thenApply(result -> result.stream().filter(AbstractMap.SimpleEntry::getValue)
        .map(AbstractMap.SimpleEntry::getKey).toList());
  }

  private UUID correctCurrentTarget(
    UUID userId, List<UUID> organizations, boolean personalBundleUsable
  ) {
    UUID newTarget;
    if (personalBundleUsable) {
      newTarget = userId;
      changeTarget(userId, newTarget);
    } else if (!organizations.isEmpty()) {
      newTarget = organizations.get(0);
      changeTarget(userId, newTarget);
    } else {
      newTarget = null;
      deleteTarget(userId);
    }
    return newTarget;
  }

  private CompletableFuture<Boolean> checkTargetUsability(UUID target) {
    return bundleDatabaseTable.bundleExists(target)
      .thenCompose(exists -> !exists ? CompletableFuture.completedFuture(false) :
        bundleDatabaseTable.findBundle(target).thenApply(bundle ->
          bundle.expiration() > System.currentTimeMillis()));
  }

  private CompletableFuture<UUID> findTarget(UUID userId) {
    return selectRow(userId).thenApply(row -> row.findCell(1).uuidValue());
  }
}

