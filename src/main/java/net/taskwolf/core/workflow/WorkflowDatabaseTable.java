package net.taskwolf.core.workflow;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class WorkflowDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "workflow";

  public static WorkflowDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("creator", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("team", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("trigger", DatabaseDataType.UUID));
    columns.add(DatabaseListColumn.create("actions", DatabaseDataType.UUID));
    columns.add(DatabaseListColumn.create("conditions", DatabaseDataType.UUID));
    columns.add(DatabaseListColumn.create("modules", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("created", DatabaseDataType.BIGINT));
    columns.add(DatabaseColumn.create("name", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("description", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("state", DatabaseDataType.TEXT));
    return new WorkflowDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private WorkflowDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertWorkflow(WorkflowEntry entry) {
    insertWorkflow(entry.id(), entry.creatorId(), entry.ownerId(), entry.teamId(),
      entry.triggerId(), entry.actionIds(), entry.conditionIds(), entry.modules(),
      entry.created(), entry.name(), entry.description(), entry.state().toString());
  }

  public void insertWorkflow(
    UUID id, UUID creatorId, UUID ownerId, UUID teamId, UUID triggerId,
    List<UUID> actionIds, List<UUID> conditionIds, List<String> modules,
    long created, String name, String description, String state
  ) {
    insert(DatabaseRow.of(id, creatorId, ownerId, teamId, triggerId, actionIds,
      conditionIds, modules, created, name, description, state));
  }

  public void updateWorkflowState(WorkflowEntry entry, WorkflowState state) {
    update(DatabaseCell.create(entry.id()), DatabaseRow.of(entry.id(),
      entry.creatorId(), entry.ownerId(), entry.teamId(), entry.triggerId(),
      entry.actionIds(), entry.conditionIds(), entry.modules(), entry.created(),
      entry.name(), entry.description(), state.toString()));
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
    return selectRows("owner=" + ownerId + " ALLOW FILTERING").thenApply(rows ->
      rows.stream().map(WorkflowEntry::of).collect(Collectors.toList()));
  }

  public CompletableFuture<List<WorkflowEntry>> findOrganizationTeamWorkflows(
    UUID organizationId, UUID teamId
  ) {
    var query = "owner=" + organizationId + " AND team=" + teamId +
      " ALLOW FILTERING";
    return selectRows(query).thenApply(rows ->
      rows.stream().map(WorkflowEntry::of).collect(Collectors.toList()));
  }

  public CompletableFuture<List<WorkflowEntry>> findGlobalOrganizationWorkflows(
    UUID organizationId
  ) {
    return selectRows("owner=" + organizationId + " AND team=NULL ALLOW FILTERING")
      .thenApply(rows -> rows.stream().map(WorkflowEntry::of)
        .collect(Collectors.toList()));
  }

  public CompletableFuture<WorkflowEntry> findWorkflowByTrigger(UUID triggerId) {
    return selectRow("trigger=" + triggerId.toString() + " ALLOW FILTERING")
      .thenApply(WorkflowEntry::of);
  }
}
