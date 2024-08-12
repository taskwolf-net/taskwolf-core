package net.taskwolf.core.workflow.timeline;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class TimelineDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "workflow_timeline";

  public static TimelineDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("workflow", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("time", DatabaseDataType.BIGINT));
    columns.add(DatabaseColumn.create("type", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("content", DatabaseDataType.TEXT));
    return new TimelineDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private TimelineDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertEntry(TimelineDatabaseEntry entry) {
    insertEntry(entry.id(), entry.workflowId(), entry.time(), entry.type(),
      entry.content());
  }

  public void insertEntry(
    UUID id, UUID workflowId, long time, String type, String content
  ) {
    insert(DatabaseRow.of(id, workflowId, time, type, content));
  }

  public void deleteEntry(UUID entryId) {
    delete(DatabaseCell.create(entryId));
  }

  public CompletableFuture<UUID> generateAvailableEntryId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    entryExists(id).thenApply(exists -> exists ?
      generateAvailableEntryId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Boolean> entryExists(UUID entryId) {
    return exists(DatabaseCell.create(entryId));
  }

  public CompletableFuture<TimelineDatabaseEntry> findEntry(UUID entryId) {
    return selectRow(DatabaseCell.create(entryId)).thenApply(TimelineDatabaseEntry::of);
  }

  public CompletableFuture<List<TimelineDatabaseEntry>> findEntriesByWorkflow(
    UUID workflowId
  ) {
    return selectRows("workflow=" + workflowId)
      .thenApply(rows -> rows.stream().map(TimelineDatabaseEntry::of)
        .collect(Collectors.toList()));
  }
}


