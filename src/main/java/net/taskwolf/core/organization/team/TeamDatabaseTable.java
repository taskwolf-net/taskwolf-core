package net.taskwolf.core.organization.team;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class TeamDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "organization_team";

  public static TeamDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("organization", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("name", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("sequence", DatabaseDataType.INT));
    columns.add(DatabaseListColumn.create("members", DatabaseDataType.UUID));
    return new TeamDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private TeamDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertTeam(Team team) {
    insertTeam(team.id(), team.organizationId(), team.name(), team.sequence(),
      team.members());
  }

  public void insertTeam(
    UUID id, UUID organizationId, String name, int sequence, List<UUID> memberIds
  ) {
    insert(DatabaseRow.of(id, organizationId, name, sequence, memberIds));
  }

  public void addTeamMember(UUID teamId, UUID memberId) {
    findTeam(teamId).thenAccept(team -> addTeamMember(team, memberId));
  }

  public void addTeamMember(Team team, UUID memberId) {
    team.addMember(memberId);
    updateTeam(team);
  }

  public void removeTeamMember(UUID teamId, UUID memberId) {
    findTeam(teamId).thenAccept(team -> removeTeamMember(team, memberId));
  }

  public void removeTeamMember(Team team, UUID memberId) {
    team.removeMember(memberId);
    updateTeam(team);
  }

  public void renameTeam(UUID teamId, String name) {
    findTeam(teamId).thenAccept(team -> renameTeam(team, name));
  }

  public void renameTeam(Team team, String name) {
    team.rename(name);
    updateTeam(team);
  }

  public void changeTeamSequence(UUID teamId, int sequence) {
    findTeam(teamId).thenAccept(team -> changeTeamSequence(team, sequence));
  }

  public void changeTeamSequence(Team team, int sequence) {
    team.changeSequence(sequence);
    updateTeam(team);
  }

  private void updateTeam(Team team) {
    update(DatabaseCell.create(team.id()), DatabaseRow.of(team.id(),
      team.organizationId(), team.name(), team.sequence(), team.members()));
  }

  public CompletableFuture<UUID> generateAvailableTeamId() {
    var id = UUID.randomUUID();
    if (id.equals(UUID.fromString("00000000-0000-0000-0000-000000000000"))) {
      return generateAvailableTeamId();
    }
    var futureResponse = new CompletableFuture<UUID>();
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

  public CompletableFuture<Team> findTeam(UUID teamId) {
    return selectRow(DatabaseCell.create(teamId)).thenApply(Team::of);
  }

  public CompletableFuture<List<Team>> findTeamsByOrganization(
    UUID organizationId
  ) {
    return selectRows("organization=" + organizationId + " ALLOW FILTERING")
      .thenApply(rows -> rows.stream().map(Team::of).toList());
  }
}
