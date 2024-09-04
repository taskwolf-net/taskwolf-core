package net.taskwolf.core.organization;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;
import net.taskwolf.core.database.condition.DatabaseCondition;
import net.taskwolf.core.target.TargetIdentificationPublish;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class OrganizationDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "organization";

  public static OrganizationDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace,
    TargetIdentificationPublish targetIdentificationPublish
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("name", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    columns.add(DatabaseListColumn.create("members", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("invitationToken", DatabaseDataType.TEXT));
    return new OrganizationDatabaseTable(connection, keyspace, TABLE_NAME, columns,
      targetIdentificationPublish);
  }

  private final TargetIdentificationPublish targetIdentificationPublish;

  private OrganizationDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns,
    TargetIdentificationPublish targetIdentificationPublish
  ) {
    super(connection, keyspace, name, columns);
    this.targetIdentificationPublish = targetIdentificationPublish;
  }

  public void insertOrganization(Organization organization) {
    insertOrganization(organization.id(), organization.name(),
      organization.owner(), organization.members(), organization.invitationToken());
  }

  public void insertOrganization(
    UUID id, String name, UUID ownerId, List<UUID> memberIds, String invitationToken
  ) {
    insert(DatabaseRow.of(id, name, ownerId, memberIds, invitationToken));
  }

  public void addOrganizationMember(UUID organizationId, UUID memberId) {
    findOrganization(organizationId).thenAccept(organization ->
      addOrganizationMember(organization, memberId));
  }

  private void addOrganizationMember(Organization organization, UUID memberId) {
    organization.addMember(memberId);
    updateOrganization(organization);
  }

  public void removeOrganizationMember(UUID organizationId, UUID memberId) {
    findOrganization(organizationId).thenAccept(organization ->
      removeOrganizationMember(organization, memberId));
  }

  private void removeOrganizationMember(Organization organization, UUID memberId) {
    organization.removeMember(memberId);
    updateOrganization(organization);
  }

  public void changeOrganizationInvitationToken(Organization organization, String token) {
    organization.changeInvitationToken(token);
    updateOrganization(organization);
  }

  public void renameOrganization(Organization organization, String name) {
    organization.rename(name);
    updateOrganization(organization);
  }

  private void updateOrganization(Organization organization) {
    update(organization.id(), DatabaseRow.of(organization.id(),
      organization.name(), organization.owner(), organization.members(),
      organization.invitationToken()));
  }

  public CompletableFuture<UUID> generateAvailableOrganizationId() {
    return targetIdentificationPublish.generateAvailableTargetId();
  }

  public CompletableFuture<Boolean> organizationExists(UUID organizationId) {
    return exists(organizationId);
  }

  public CompletableFuture<Boolean> organizationExistsByOwner(UUID ownerId) {
    return exists(DatabaseCondition.of("owner", ownerId));
  }

  public void deleteOrganization(UUID organizationId) {
    delete(organizationId);
  }

  public CompletableFuture<Organization> findOrganization(UUID organizationId) {
    return selectRow(organizationId).thenApply(Organization::of);
  }

  public CompletableFuture<Organization> findOrganizationByOwner(UUID ownerId) {
    return selectRow(DatabaseCondition.of("owner", ownerId))
      .thenApply(Organization::of);
  }
}
