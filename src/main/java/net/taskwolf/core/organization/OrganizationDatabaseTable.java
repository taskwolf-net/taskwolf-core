package net.taskwolf.core.organization;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class OrganizationDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "organization";

  public static OrganizationDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("name", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    columns.add(DatabaseListColumn.create("members", DatabaseDataType.UUID));
    columns.add(DatabaseListColumn.create("invitations", DatabaseDataType.UUID));
    return new OrganizationDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private OrganizationDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertOrganization(Organization organization) {
    insertOrganization(organization.id(), organization.name(),
      organization.owner(), organization.members(), organization.invitations());
  }

  public void insertOrganization(
    UUID id, String name, UUID ownerId, List<UUID> memberIds, List<UUID> invitations
  ) {
    insert(DatabaseRow.of(id, name, ownerId, memberIds, invitations));
  }

  public void acceptOrganizationInvitation(UUID organizationId, UUID userId) {
    findOrganization(organizationId).thenAccept(organization ->
      acceptOrganizationInvitation(organization, userId));
  }

  private void acceptOrganizationInvitation(Organization organization, UUID userId) {
    organization.addMember(userId);
    organization.removeInvitation(userId);
    updateOrganization(organization);
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

  public void addOrganizationInvitation(UUID organizationId, UUID userId) {
    findOrganization(organizationId).thenAccept(organization ->
      addOrganizationInvitation(organization, userId));
  }

  private void addOrganizationInvitation(Organization organization, UUID userId) {
    organization.addInvitation(userId);
    updateOrganization(organization);
  }

  public void removeOrganizationInvitation(UUID organizationId, UUID userId) {
    findOrganization(organizationId).thenAccept(organization ->
      removeOrganizationInvitation(organization, userId));
  }

  private void removeOrganizationInvitation(Organization organization, UUID userId) {
    organization.removeInvitation(userId);
    updateOrganization(organization);
  }

  private void updateOrganization(Organization organization) {
    update(DatabaseCell.create(organization.id()), DatabaseRow.of(organization.id(),
      organization.name(), organization.owner(), organization.members(),
      organization.invitations()));
  }

  public CompletableFuture<UUID> generateAvailableOrganizationId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    organizationExists(id).thenApply(exists -> exists ?
      generateAvailableOrganizationId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Boolean> organizationExists(UUID organizationId) {
    return exists(DatabaseCell.create(organizationId));
  }

  public void deleteOrganization(UUID organizationId) {
    delete(DatabaseCell.create(organizationId));
  }

  public CompletableFuture<Organization> findOrganization(UUID organizationId) {
    return selectRow(DatabaseCell.create(organizationId)).thenApply(Organization::of);
  }

  public CompletableFuture<List<Organization>> findAllOrganization() {
    return selectAllRows().thenApply(rows ->
      rows.stream().map(Organization::of).collect(Collectors.toList()));
  }
}
