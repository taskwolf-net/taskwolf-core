package net.taskwolf.core.trigger;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class TriggerDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "triggers";

  public static TriggerDatabaseTable create(
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
    columns.add(DatabaseColumn.create("state", DatabaseDataType.TEXT));
    return new TriggerDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private TriggerDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertTrigger(TriggerEntry entry) {
    insertTrigger(entry.id(), entry.ownerId(), entry.workflowId(),
      entry.module(), entry.type(), entry.content(), entry.state().toString());
  }

  public void insertTrigger(
    UUID id, UUID ownerId, UUID workflowId, String module, String type,
    String content, String state
  ) {
    insert(DatabaseRow.of(id, ownerId, workflowId, module, type, content, state));
  }

  public void deleteTrigger(UUID triggerId) {
    delete(DatabaseCell.create(triggerId));
  }

  public CompletableFuture<UUID> generateAvailableTriggerId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    triggerExists(id).thenApply(exists -> exists ?
      generateAvailableTriggerId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Boolean> triggerExists(UUID triggerId) {
    return exists(DatabaseCell.create(triggerId));
  }

  public CompletableFuture<TriggerEntry> findTrigger(UUID triggerId) {
    return selectRow(DatabaseCell.create(triggerId)).thenApply(TriggerEntry::of);
  }

  public CompletableFuture<TriggerEntry> findTriggerByWorkflow(UUID workflowId) {
    return selectRow("workflow=" + workflowId).thenApply(TriggerEntry::of);
  }

  public CompletableFuture<List<TriggerEntry>> findTriggersByModuleAndType(
    String module, String type
  ) {
    return selectRows("module='" + module + "' AND type='" + type + "'")
      .thenApply(rows -> rows.stream().map(TriggerEntry::of)
        .collect(Collectors.toList()));
  }
}
