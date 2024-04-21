package net.taskwolf.core.condition;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class ConditionDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "condition";

  public static ConditionDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("workflow", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("actionIndex", DatabaseDataType.INT));
    columns.add(DatabaseColumn.create("conditionIndex", DatabaseDataType.INT));
    columns.add(DatabaseColumn.create("type", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("content", DatabaseDataType.TEXT));
    return new ConditionDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private ConditionDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertCondition(ConditionEntry entry) {
    insertCondition(entry.id(), entry.ownerId(), entry.workflowId(),
      entry.actionIndex(), entry.conditionIndex(), entry.type(), entry.content());
  }

  public void insertCondition(
    UUID id, UUID ownerId, UUID workflowId, int actionIndex, int conditionIndex,
    String type, String content
  ) {
    insert(DatabaseRow.of(id, ownerId, workflowId, actionIndex, conditionIndex,
      type, content));
  }

  public void deleteCondition(UUID conditionId) {
    delete(DatabaseCell.create(conditionId));
  }

  public CompletableFuture<UUID> generateAvailableConditionId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    conditionExists(id).thenApply(exists -> exists ?
      generateAvailableConditionId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Boolean> conditionExists(UUID conditionId) {
    return exists(DatabaseCell.create(conditionId));
  }

  public CompletableFuture<ConditionEntry> findCondition(UUID conditionId) {
    return selectRow(DatabaseCell.create(conditionId)).thenApply(ConditionEntry::of);
  }

  public CompletableFuture<List<ConditionEntry>> findConditionsByWorkflow(
    UUID workflowId
  ) {
    return selectRows("workflow=" + workflowId + " ALLOW FILTERING")
      .thenApply(rows -> rows.stream().map(ConditionEntry::of)
        .collect(Collectors.toList()));
  }
}

