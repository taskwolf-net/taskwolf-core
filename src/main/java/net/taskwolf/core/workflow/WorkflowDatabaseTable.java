package net.taskwolf.core.workflow;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class WorkflowDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "workflow";

  public static WorkflowDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID,
      DatabaseColumn.Type.PARTITION_KEY));
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.CLUSTERING_KEY, DatabaseOrder.ASCENDING));
    columns.add(DatabaseColumn.create("creator", DatabaseDataType.UUID));
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
    insertWorkflow(entry.ownerId(), entry.id(), entry.creatorId(), entry.triggerId(),
      entry.actionIds(), entry.conditionIds(), entry.modules(), entry.created(),
      entry.name(), entry.description(), entry.state().toString());
  }

  public void insertWorkflow(
    UUID id, UUID ownerId, UUID creatorId, UUID triggerId, List<UUID> actionIds,
    List<UUID> conditionIds, List<String> modules, long created, String name,
    String description, String state
  ) {
    insert(DatabaseRow.of(ownerId, id, creatorId, triggerId, actionIds,
      conditionIds, modules, created, name, description, state));
  }

  public void updateWorkflowState(WorkflowEntry entry, WorkflowState state) {
    update("owner=" + entry.ownerId() + " AND id=" + entry.id(),
      DatabaseRow.of(entry.ownerId(), entry.id(), entry.creatorId(),
        entry.triggerId(), entry.actionIds(), entry.conditionIds(),
        entry.modules(), entry.created(), entry.name(), entry.description(),
        state.toString()));
  }

  public void deleteWorkflow(UUID workflowId) {
    findWorkflow(workflowId).thenAccept(workflow ->
      delete("owner=" + workflow.ownerId() + " AND id=" + workflow.id()));
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
    return exists("id=" + workflowId);
  }

  public CompletableFuture<WorkflowEntry> findWorkflow(UUID workflowId) {
    return selectRow("id=" + workflowId).thenApply(WorkflowEntry::of);
  }

  private static final int PAGE_SIZE = 5;

  public CompletableFuture<DatabasePage<WorkflowEntry>> findWorkflowsOfOwner(
    UUID ownerId, int page
  ) {
    return selectPage(PAGE_SIZE, page, "owner=" + ownerId)
      .thenApply(result -> DatabasePage.create(result.content().stream()
        .map(WorkflowEntry::of).toList(), result.pageState()));
  }

  public CompletableFuture<DatabasePage<WorkflowEntry>> findWorkflowsOfOwner(
    UUID ownerId, String currentPageState, DatabaseDirection direction
  ) {
    return shiftPage(DatabaseCell.create(ownerId), "id", DatabaseOrder.ASCENDING,
      PAGE_SIZE, currentPageState, direction)
      .thenApply(result -> DatabasePage.create(result.content().stream()
        .map(WorkflowEntry::of).toList(), result.pageState()));
  }

  public CompletableFuture<Long> findWorkflowPages(UUID ownerId) {
    return findWorkflowCount(ownerId)
      .thenApply(count -> (long) Math.ceil(count.doubleValue() / PAGE_SIZE));
  }

  public CompletableFuture<Long> findWorkflowCount(UUID ownerId) {
    return count("owner=" + ownerId);
  }

  public CompletableFuture<List<WorkflowEntry>> findAllWorkflowsOfOwner(
    UUID ownerId
  ) {
    return selectRows("owner=" + ownerId)
      .thenApply(rows -> rows.stream().map(WorkflowEntry::of).toList());
  }

  public CompletableFuture<List<WorkflowEntry>> findWorkflowByModule(
    UUID ownerId, String module
  ) {
    var query = "owner=" + ownerId + " AND modules CONTAINS '" + module + "' " +
      "ALLOW FILTERING";
    return selectRows(query.toString())
      .thenApply(rows -> rows.stream().map(WorkflowEntry::of).toList());
  }

  public CompletableFuture<WorkflowEntry> findWorkflowByTrigger(UUID triggerId) {
    return selectRow("trigger=" + triggerId.toString())
      .thenApply(WorkflowEntry::of);
  }
}
