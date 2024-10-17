package com.dulno.core.user;

import com.dulno.core.database.*;
import com.google.common.collect.Lists;
import com.dulno.core.database.condition.DatabaseCondition;
import com.dulno.core.target.TargetIdentificationPublish;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class UserDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "user";

  public static UserDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace,
    TargetIdentificationPublish targetIdentificationPublish
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("name", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("email", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("password", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("language", DatabaseDataType.TEXT));
    columns.add(DatabaseListColumn.create("organizations", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("legalAccepted", DatabaseDataType.BOOLEAN));
    columns.add(DatabaseColumn.create("newsletter", DatabaseDataType.BOOLEAN));
    return new UserDatabaseTable(connection, keyspace, TABLE_NAME, columns,
      targetIdentificationPublish);
  }

  private final TargetIdentificationPublish targetIdentificationPublish;

  private UserDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns,
    TargetIdentificationPublish targetIdentificationPublish
  ) {
    super(connection, keyspace, name, columns);
    this.targetIdentificationPublish = targetIdentificationPublish;
  }

  public CompletableFuture<Void> insertUser(User user) {
    return insertUser(user.id(), user.name(), user.email(), user.passwordHash(),
      user.language(), user.organizations(), user.legalAccepted(),
      user.newsletter());
  }

  public CompletableFuture<Void> insertUser(
    UUID id, String name, String email, String passwordHash, String language,
    List<UUID> organizations, boolean legalAccepted, boolean newsletter
  ) {
    return insert(DatabaseRow.of(id, name, email.toLowerCase(), passwordHash,
      language, organizations, legalAccepted, newsletter));
  }

  public CompletableFuture<Void> addUserOrganization(
    UUID userId, UUID organizationId
  ) {
    return findUser(userId).thenCompose(user ->
      addUserOrganization(user, organizationId));
  }

  private CompletableFuture<Void> addUserOrganization(
    User user, UUID organizationId
  ) {
    user.addOrganization(organizationId);
    return updateUser(user);
  }

  public CompletableFuture<Void> removeUserOrganization(
    UUID userId, UUID organizationId
  ) {
    return findUser(userId).thenCompose(user ->
      removeUserOrganization(user, organizationId));
  }

  private CompletableFuture<Void> removeUserOrganization(
    User user, UUID organizationId
  ) {
    user.removeOrganization(organizationId);
    return updateUser(user);
  }

  public CompletableFuture<Void> changeUserName(UUID userId, String newName) {
    return findUser(userId).thenCompose(user -> changeUserName(user, newName));
  }

  private CompletableFuture<Void> changeUserName(User user, String newName) {
    user.changeName(newName);
    return updateUser(user);
  }

  public CompletableFuture<Void> changeUserEmail(UUID userId, String newEmail) {
    return findUser(userId).thenCompose(user -> changeUserEmail(user, newEmail));
  }

  private CompletableFuture<Void> changeUserEmail(User user, String newEmail) {
    user.changeEmail(newEmail);
    return updateUser(user);
  }

  public CompletableFuture<Void> changeUserPassword(
    UUID userId, String newPasswordHash
  ) {
    return findUser(userId).thenCompose(user ->
      changeUserPassword(user, newPasswordHash));
  }

  private CompletableFuture<Void> changeUserPassword(
    User user, String newPasswordHash
  ) {
    user.changePassword(newPasswordHash);
    return updateUser(user);
  }

  public CompletableFuture<Void>  changeUserLanguage(UUID userId, String newLanguage) {
    return findUser(userId).thenCompose(user -> changeUserLanguage(user, newLanguage));
  }

  private CompletableFuture<Void> changeUserLanguage(User user, String newLanguage) {
    user.changeLanguage(newLanguage);
    return updateUser(user);
  }

  private CompletableFuture<Void> updateUser(User user) {
    return update(user.id(), DatabaseRow.of(user.id(), user.name(),
      user.email().toLowerCase(), user.passwordHash(), user.language(),
      user.organizations(), user.legalAccepted(), user.newsletter()));
  }

  public CompletableFuture<UUID> generateAvailableUserId() {
    return targetIdentificationPublish.generateAvailableTargetId();
  }

  public CompletableFuture<Boolean> userExists(UUID userId) {
    return exists(userId);
  }

  public CompletableFuture<Boolean> userExists(String email) {
    return exists(DatabaseCondition.of("email", email.toLowerCase()));
  }

  public CompletableFuture<Void> deleteUser(UUID userId) {
    return delete(userId);
  }

  public CompletableFuture<User> findUser(UUID userId) {
    return selectRow(userId).thenApply(User::of);
  }

  public CompletableFuture<User> findUserIfExists(UUID userId) {
    var futureResponse = new CompletableFuture<User>();
    userExists(userId).thenAccept(exists -> completeExistenceUserFinding(
      userId, exists).thenAccept(futureResponse::complete));
    return futureResponse;
  }

  private CompletableFuture<User> completeExistenceUserFinding(
    UUID userId, boolean exists
  ) {
    if (!exists) {
      return CompletableFuture.completedFuture(User.unknown(userId));
    }
    return findUser(userId);
  }

  public CompletableFuture<User> findUser(String email) {
    return selectRow(DatabaseCondition.of("email", email.toLowerCase()))
      .thenApply(User::of);
  }

  public CompletableFuture<List<User>> findAllUsers() {
    return selectAllRows().thenApply(rows ->
      rows.stream().map(User::of).toList());
  }
}
