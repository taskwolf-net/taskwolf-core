package net.taskwolf.core.user;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;
import net.taskwolf.core.database.condition.DatabaseCondition;
import net.taskwolf.core.target.TargetIdentificationPublish;

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

  public void insertUser(User user) {
    insertUser(user.id(), user.name(), user.email(), user.passwordHash(),
      user.language(), user.organizations());
  }

  public void insertUser(
    UUID id, String name, String email, String passwordHash, String language,
    List<UUID> organizations
  ) {
    insert(DatabaseRow.of(id, name, email.toLowerCase(), passwordHash,
      language, organizations));
  }

  public void addUserOrganization(UUID userId, UUID organizationId) {
    findUser(userId).thenAccept(user -> addUserOrganization(user, organizationId));
  }

  private void addUserOrganization(User user, UUID organizationId) {
    user.addOrganization(organizationId);
    updateUser(user);
  }

  public void removeUserOrganization(UUID userId, UUID organizationId) {
    findUser(userId).thenAccept(user -> removeUserOrganization(user, organizationId));
  }

  private void removeUserOrganization(User user, UUID organizationId) {
    user.removeOrganization(organizationId);
    updateUser(user);
  }

  public void changeUserName(UUID userId, String newName) {
    findUser(userId).thenAccept(user -> changeUserName(user, newName));
  }

  private void changeUserName(User user, String newName) {
    user.changeName(newName);
    updateUser(user);
  }

  public void changeUserEmail(UUID userId, String newEmail) {
    findUser(userId).thenAccept(user -> changeUserEmail(user, newEmail));
  }

  private void changeUserEmail(User user, String newEmail) {
    user.changeEmail(newEmail);
    updateUser(user);
  }

  public void changeUserPassword(UUID userId, String newPasswordHash) {
    findUser(userId).thenAccept(user -> changeUserPassword(user, newPasswordHash));
  }

  private void changeUserPassword(User user, String newPasswordHash) {
    user.changePassword(newPasswordHash);
    updateUser(user);
  }

  public void changeUserLanguage(UUID userId, String newLanguage) {
    findUser(userId).thenAccept(user -> changeUserLanguage(user, newLanguage));
  }

  private void changeUserLanguage(User user, String newLanguage) {
    user.changeLanguage(newLanguage);
    updateUser(user);
  }

  private void updateUser(User user) {
    update(user.id(), DatabaseRow.of(user.id(), user.name(),
      user.email().toLowerCase(), user.passwordHash(), user.language(),
      user.organizations()));
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

  public void deleteUser(UUID userId) {
    delete(userId);
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
}
