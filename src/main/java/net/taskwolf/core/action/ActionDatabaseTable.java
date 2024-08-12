package net.taskwolf.core.action;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class ActionDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "action";

  public static ActionDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("workflow", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("actionIndex", DatabaseDataType.INT));
    columns.add(DatabaseColumn.create("module", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("type", DatabaseDataType.TEXT));
    return new ActionDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private ActionDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertAction(ActionEntry entry) {
    insertAction(entry.id(), entry.ownerId(), entry.workflowId(),
      entry.actionIndex(), entry.module(), entry.type());
  }

  public void insertAction(
    UUID id, UUID ownerId, UUID workflowId, int actionIndex, String module,
    String type
  ) {
    insert(DatabaseRow.of(id, ownerId, workflowId, actionIndex, module, type));
  }

  public void deleteAction(UUID actionId) {
    delete(DatabaseCell.create(actionId));
  }

  public CompletableFuture<UUID> generateAvailableActionId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    actionExists(id).thenApply(exists -> exists ?
      generateAvailableActionId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Boolean> actionExists(UUID actionId) {
    return exists(DatabaseCell.create(actionId));
  }

  public CompletableFuture<ActionEntry> findAction(UUID actionId) {
    return selectRow(DatabaseCell.create(actionId)).thenApply(ActionEntry::of);
  }

  public CompletableFuture<List<ActionEntry>> findActionsByWorkflow(
    UUID workflowId
  ) {
    return selectRows("workflow=" + workflowId)
      .thenApply(rows -> rows.stream().map(ActionEntry::of)
        .collect(Collectors.toList()));
  }
}
