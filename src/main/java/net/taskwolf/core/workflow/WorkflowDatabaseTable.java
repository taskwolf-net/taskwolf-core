package net.taskwolf.core.workflow;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class WorkflowDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "workflows";

  public static WorkflowDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("creator", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("affiliation", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("trigger", DatabaseDataType.UUID));
    columns.add(DatabaseListColumn.create("actions", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("name", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("description", DatabaseDataType.TEXT));
    return new WorkflowDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private WorkflowDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertWorkflow(WorkflowEntry entry) {
    insertWorkflow(entry.id(), entry.creatorId(), entry.affiliation().toString(),
      entry.ownerId(), entry.triggerId(), entry.actionIds(), entry.name(),
      entry. description());
  }

  public void insertWorkflow(
    UUID id, UUID creatorId, String affiliation, UUID ownerId,
    UUID triggerId, List<UUID> actionIds, String name, String description
  ) {
    insert(DatabaseRow.of(id, creatorId, affiliation, ownerId, triggerId, actionIds,
      name, description));
  }

  public void deleteWorkflow(UUID workflowId) {
    delete(DatabaseCell.create(workflowId));
  }

  public CompletableFuture<UUID> generateAvailableWorkflowId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    workflowExists(id).thenApply(exists -> exists ?
      generateAvailableWorkflowId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Boolean> workflowExists(UUID workflowId) {
    return exists(DatabaseCell.create(workflowId));
  }

  public CompletableFuture<WorkflowEntry> findWorkflow(UUID workflowId) {
    return selectRow(DatabaseCell.create(workflowId)).thenApply(WorkflowEntry::of);
  }

  public CompletableFuture<List<WorkflowEntry>> findWorkflowsOfOwner(UUID ownerId) {
    return selectRows("owner=" + ownerId  + " ALLOW FILTERING").thenApply(rows ->
      rows.stream().map(WorkflowEntry::of).collect(Collectors.toList()));
  }

  public CompletableFuture<WorkflowEntry> findWorkflowByTrigger(UUID triggerId) {
    return selectRow("trigger=" + triggerId.toString())
      .thenApply(WorkflowEntry::of);
  }
}
