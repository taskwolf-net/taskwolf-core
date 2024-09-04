package net.taskwolf.core.organization.team;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class TeamTargetDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "organization_team_target";

  public static TeamTargetDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace,
    TeamDatabaseTable teamDatabaseTable
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("target", DatabaseDataType.UUID));
    return new TeamTargetDatabaseTable(connection, keyspace, TABLE_NAME,
      columns, teamDatabaseTable);
  }

  private final TeamDatabaseTable teamDatabaseTable;

  private TeamTargetDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns,
    TeamDatabaseTable teamDatabaseTable
  ) {
    super(connection, keyspace, name, columns);
    this.teamDatabaseTable = teamDatabaseTable;
  }

  public void insertTarget(UUID userId, UUID target) {
    insert(DatabaseRow.of(userId, target));
  }

  public void changeTarget(UUID userId, UUID target) {
    update(userId, DatabaseRow.of(userId, target));
  }

  public CompletableFuture<Boolean> targetExists(UUID userId) {
    return exists(userId);
  }

  public void deleteTarget(UUID userId) {
    delete(userId);
  }

  public CompletableFuture<Optional<UUID>> findTargetSecured(UUID userId) {
    return targetExists(userId)
      .thenCompose(exists -> checkTargetValidity(userId, exists));
  }

  private CompletableFuture<Optional<UUID>> checkTargetValidity(
    UUID userId, boolean targetExists
  ) {
    if (!targetExists) {
      return CompletableFuture.completedFuture(Optional.empty());
    }
    return findTarget(userId)
      .thenCompose(target -> teamDatabaseTable.teamExists(target)
        .thenCompose(exists -> checkTargetTeamExistence(userId, target, exists)));
  }

  private CompletableFuture<Optional<UUID>> checkTargetTeamExistence(
    UUID userId, UUID target, boolean teamExists
  ) {
    if (!teamExists) {
      deleteTarget(userId);
      return CompletableFuture.completedFuture(Optional.empty());
    }
    return teamDatabaseTable.findTeam(target).thenApply(
      organization -> checkTargetOrganizationPermission(userId, organization));
  }

  private Optional<UUID> checkTargetOrganizationPermission(
    UUID userId, Team team
  ) {
    if (!team.members().contains(userId)) {
      deleteTarget(userId);
      return Optional.empty();
    }
    return Optional.of(team.id());
  }

  public CompletableFuture<UUID> findTarget(UUID userId) {
    return selectRow(userId).thenApply(row ->
      row.findCell(1).uuidValue());
  }
}

