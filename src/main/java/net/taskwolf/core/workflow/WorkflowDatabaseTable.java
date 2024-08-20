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
      DatabaseColumn.Type.CLUSTERING_KEY));
    columns.add(DatabaseColumn.create("creator", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("trigger", DatabaseDataType.UUID));
    columns.add(DatabaseListColumn.create("actions", DatabaseDataType.UUID));
    columns.add(DatabaseListColumn.create("conditions", DatabaseDataType.UUID));
    columns.add(DatabaseListColumn.create("modules", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("created", DatabaseDataType.BIGINT));
    columns.add(DatabaseColumn.create("name", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("description", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("state", DatabaseDataType.TEXT));
    var table = new WorkflowDatabaseTable(connection, keyspace, TABLE_NAME, columns);
    table.createIfNotExists();
    table.createIndexIfNotExists("id");
    table.createIndexIfNotExists("trigger");
    table.createIndexIfNotExists("modules");
    table.createIndexIfNotExists("name",
      "'org.apache.cassandra.index.sasi.SASIIndex' WITH OPTIONS = " +
        "{'mode': 'CONTAINS', 'analyzer_class': " +
        "'org.apache.cassandra.index.sasi.analyzer.StandardAnalyzer', " +
        "'case_sensitive': 'false'}");
    table.initializeViews();
    return table;
  }

  private DatabaseTable nameView;
  private DatabaseTable creatorView;
  private DatabaseTable createdView;
  private DatabaseTable stateView;

  private WorkflowDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  private void initializeViews() {
    nameView = createMaterializedViewIfNotExists("name_view", "name");
    creatorView = createMaterializedViewIfNotExists("creator_view", "creator");
    createdView = createMaterializedViewIfNotExists("created_view", "created");
    stateView = createMaterializedViewIfNotExists("state_view", "state");
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
    UUID ownerId, int targetPage, String sortingColumn, DatabaseOrder sortingOrder,
    String search, UUID creatorId, long startTime, long endTime, String state
  ) {
    if (!search.isEmpty()) {
      return selectRows("owner=" + ownerId + " AND name LIKE '%" + search +
        "%' LIMIT " + PAGE_SIZE)
        .thenApply(rows -> createWorkflowPage(DatabasePage.create(rows, "", 1)));
    }
    return findTargetView(sortingColumn).selectPage(DatabaseCell.create(ownerId),
        createWorkflowsConditions(creatorId, startTime, endTime, state),
        sortingOrder, PAGE_SIZE, targetPage)
      .thenApply(this::createWorkflowPage);
  }

  public CompletableFuture<DatabasePage<WorkflowEntry>> findWorkflowsOfOwner(
    UUID ownerId, String pageState, DatabaseDirection startingPoint,
    DatabaseDirection direction, String sortingColumn, DatabaseOrder sortingOrder,
    UUID creatorId, long startTime, long endTime, String state
  ) {
    return findTargetView(sortingColumn).shiftPage(DatabaseCell.create(ownerId),
        createWorkflowsConditions(creatorId, startTime, endTime, state),
        sortingOrder, PAGE_SIZE, pageState, startingPoint, direction)
      .thenApply(this::createWorkflowPage);
  }

  private DatabaseTable findTargetView(String sortingColumn) {
    if (sortingColumn.equals("name")) {
      return nameView;
    } else if (sortingColumn.equals("creator")) {
      return creatorView;
    } else if (sortingColumn.equals("created")) {
      return createdView;
    } else if (sortingColumn.equals("state")) {
      return stateView;
    }
    return null;
  }

  private List<String> createWorkflowsConditions(
    UUID creatorId, long startTime, long endTime, String state
  ) {
    var conditions = Lists.<String>newArrayList();
    if (creatorId != null) {
      conditions.add("creator = " + creatorId);
    }
    if (startTime > 0) {
      conditions.add("created >= " + startTime);
    }
    if (endTime > 0) {
      conditions.add("created <= " + endTime);
    }
    if (state != null) {
      conditions.add("state = " + state);
    }
    return conditions;
  }

  private DatabasePage<WorkflowEntry> createWorkflowPage(
    DatabasePage<DatabaseRow> page
  ) {
    return DatabasePage.create(page.content().stream().map(WorkflowEntry::of).toList(),
      page.pageState(), page.pageNumber());
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
