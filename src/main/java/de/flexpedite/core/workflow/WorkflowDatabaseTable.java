package de.flexpedite.core.workflow;

import com.google.common.collect.Lists;
import de.flexpedite.core.database.*;
import de.flexpedite.core.trigger.TriggerEntry;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class WorkflowDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "workflows";

  public static WorkflowDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    List<DatabaseColumn> columns = Lists.newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("user", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("trigger", DatabaseDataType.UUID));
    columns.add(DatabaseListColumn.create("actions", DatabaseDataType.UUID));
    return new WorkflowDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private WorkflowDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertWorkflow(WorkflowEntry entry) {
    insertWorkflow(entry.id(), entry.userId(), entry.triggerId(), entry.actionIds());
  }

  public void insertWorkflow(
    UUID id, UUID userId, UUID triggerId, List<UUID> actionIds
  ) {
    insert(DatabaseRow.of(id, userId, triggerId, actionIds));
  }

  public void deleteWorkflow(UUID workflowId) {
    delete(DatabaseCell.create(workflowId));
  }

  public CompletableFuture<WorkflowEntry> findWorkflow(UUID workflowId) {
    return selectRow(DatabaseCell.create(workflowId)).thenApply(WorkflowEntry::of);
  }

  public CompletableFuture<WorkflowEntry> findWorkflowByTrigger(UUID triggerId) {
    return selectRow("trigger='" + triggerId.toString() + "'")
      .thenApply(WorkflowEntry::of);
  }
}
