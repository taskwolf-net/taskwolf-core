package de.flexpedite.core.trigger;

import com.google.common.collect.Lists;
import de.flexpedite.core.database.*;

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
    columns.add(DatabaseColumn.create("user", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("workflow", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("module", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("type", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("content", DatabaseDataType.TEXT));
    return new TriggerDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private TriggerDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertTrigger(TriggerEntry entry) {
    insertTrigger(entry.id(), entry.userId(), entry.workflowId(),
      entry.module(), entry.type(), entry.content());
  }

  public void insertTrigger(
    UUID id, UUID userId, UUID workflowId, String module, String type,
    String content
  ) {
    insert(DatabaseRow.of(id, userId, workflowId, module, type, content));
  }

  public void deleteTrigger(UUID triggerId) {
    delete(DatabaseCell.create(triggerId));
  }

  public CompletableFuture<TriggerEntry> findTrigger(UUID triggerId) {
    return selectRow(DatabaseCell.create(triggerId)).thenApply(TriggerEntry::of);
  }

  public CompletableFuture<TriggerEntry> findTriggerByWorkflow(UUID workflowId) {
    return selectRow("workflow='" + workflowId + "'").thenApply(TriggerEntry::of);
  }

  public CompletableFuture<List<TriggerEntry>> findTriggersByModuleAndType(
    String module, String type
  ) {
    return selectRows("module='" + module + "' AND type='" + type + "'")
      .thenApply(rows -> rows.stream().map(TriggerEntry::of)
        .collect(Collectors.toList()));
  }
}
