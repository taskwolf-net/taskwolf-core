package net.taskwolf.core.team;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class TeamDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "team";

  public static TeamDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("group", DatabaseDataType.TEXT));
    return new TeamDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private TeamDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertTeamMember(TeamMember teamMember) {
    insertTeamMember(teamMember.userId(), teamMember.group());
  }

  public void insertTeamMember(UUID id, String group) {
    insert(DatabaseRow.of(id, group));
  }

  public void deleteTeamMember(UUID id) {
    delete(DatabaseCell.create(id));
  }

  public CompletableFuture<Boolean> teamMemberExists(UUID id) {
    return exists(DatabaseCell.create(id));
  }

  public CompletableFuture<TeamMember> findTeamMember(UUID id) {
    return selectRow(DatabaseCell.create(id)).thenApply(TeamMember::of);
  }
}

