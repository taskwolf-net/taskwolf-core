package net.taskwolf.core.organization;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class InvitationDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "invitations";

  public static InvitationDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("user", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseListColumn.create("organizations", DatabaseDataType.UUID));
    return new InvitationDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private InvitationDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void addInvitation(UUID userId, UUID organizationId) {
    exists(DatabaseCell.create(userId)).thenAccept(exists ->
      addInvitation(userId, organizationId, exists));
  }

  private void addInvitation(UUID userId, UUID organizationId, boolean exists) {
    if (!exists) {
      insertInvitation(userId, organizationId);
      return;
    }
    selectRow(DatabaseCell.create(userId)).thenAccept(row ->
      addInvitation(userId, organizationId, row));
  }

  private void addInvitation(UUID userId, UUID organizationId, DatabaseRow row) {
    var organizationIds = row.findCell(1).<UUID>listValue();
    organizationIds.add(organizationId);
    updateInvitations(userId, organizationIds);
  }

  private void insertInvitation(UUID userId, UUID organizationId) {
    insert(DatabaseRow.of(userId, Lists.newArrayList(organizationId)));
  }

  public void removeInvitation(UUID userId, UUID organizationId) {
    selectRow(DatabaseCell.create(userId)).thenAccept(row ->
      removeInvitation(userId, organizationId, row));
  }

  private void removeInvitation(UUID userId, UUID organizationId, DatabaseRow row) {
    var organizationIds = row.findCell(1).<UUID>listValue();
    if (organizationIds.size() == 1) {
      deleteInvitations(userId);
      return;
    }
    organizationIds.remove(organizationId);
    updateInvitations(userId, organizationIds);
  }

  private void updateInvitations(UUID userId, List<UUID> organizationIds) {
    update(DatabaseCell.create(userId), DatabaseRow.of(userId, organizationIds));
  }

  public void deleteInvitations(UUID userId) {
    delete(DatabaseCell.create(userId));
  }

  public CompletableFuture<List<UUID>> findInvitations(UUID userId) {
    return selectRow(DatabaseCell.create(userId))
      .thenApply(row -> row.findCell(1).listValue());
  }
}
