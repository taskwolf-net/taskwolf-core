package net.taskwolf.core.organization;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class OrganizationTeamDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "organization_team";

  public static OrganizationTeamDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("organization", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("name", DatabaseDataType.TEXT));
    columns.add(DatabaseListColumn.create("members", DatabaseDataType.UUID));
    return new OrganizationTeamDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private OrganizationTeamDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertTeam(OrganizationTeam team) {
    insertTeam(team.id(), team.organizationId(), team.name(), team.members());
  }

  public void insertTeam(
    UUID id, UUID organizationId, String name, List<UUID> memberIds
  ) {
    insert(DatabaseRow.of(id, organizationId, name, memberIds));
  }

  public void addTeamMember(UUID teamId, UUID memberId) {
    findTeam(teamId).thenAccept(team -> addTeamMember(team, memberId));
  }

  public void addTeamMember(OrganizationTeam team, UUID memberId) {
    team.addMember(memberId);
    updateTeam(team);
  }

  public void removeTeamMember(UUID teamId, UUID memberId) {
    findTeam(teamId).thenAccept(team -> removeTeamMember(team, memberId));
  }

  public void removeTeamMember(OrganizationTeam team, UUID memberId) {
    team.removeMember(memberId);
    updateTeam(team);
  }

  public void renameTeam(UUID teamId, String name) {
    findTeam(teamId).thenAccept(team -> renameTeam(team, name));
  }

  public void renameTeam(OrganizationTeam team, String name) {
    team.rename(name);
    updateTeam(team);
  }

  private void updateTeam(OrganizationTeam team) {
    update(DatabaseCell.create(team.id()), DatabaseRow.of(team.id(),
      team.organizationId(), team.name(), team.members()));
  }

  public CompletableFuture<UUID> generateAvailableTeamId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    teamExists(id).thenApply(exists -> exists ?
      generateAvailableTeamId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Boolean> teamExists(UUID teamId) {
    return exists(DatabaseCell.create(teamId));
  }

  public CompletableFuture<Boolean> teamExistsByOrganization(UUID organizationId) {
    return exists("organization=" + organizationId + " ALLOW FILTERING");
  }

  public void deleteTeam(UUID teamId) {
    delete(DatabaseCell.create(teamId));
  }

  public CompletableFuture<OrganizationTeam> findTeam(UUID teamId) {
    return selectRow(DatabaseCell.create(teamId)).thenApply(OrganizationTeam::of);
  }

  public CompletableFuture<OrganizationTeam> findTeamByOrganization(
    UUID organizationId
  ) {
    return selectRow("organization=" + organizationId + " ALLOW FILTERING")
      .thenApply(OrganizationTeam::of);
  }
}
