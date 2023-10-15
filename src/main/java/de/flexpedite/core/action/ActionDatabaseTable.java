package de.flexpedite.core.action;

import com.google.common.collect.Lists;
import de.flexpedite.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class ActionDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "actions";

  public static ActionDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("workflow", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("module", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("type", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("content", DatabaseDataType.TEXT));
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
      entry.module(), entry.type(), entry.content());
  }

  public void insertAction(
    UUID id, UUID ownerId, UUID workflowId, String module, String type,
    String content
  ) {
    insert(DatabaseRow.of(id, ownerId, workflowId, module, type, content));
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

  public CompletableFuture<List<ActionEntry>> findActionsByModuleAndType(
    String module, String type
  ) {
    return selectRows("module='" + module + "' AND type='" + type + "'")
      .thenApply(rows -> rows.stream().map(ActionEntry::of)
        .collect(Collectors.toList()));
  }
}
