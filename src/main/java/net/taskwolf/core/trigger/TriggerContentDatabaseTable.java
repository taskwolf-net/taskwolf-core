package net.taskwolf.core.trigger;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class TriggerContentDatabaseTable extends DatabaseTable {
  public static TriggerContentDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace,
    String tableName, List<DatabaseColumn> contentColumns
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("trigger", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.addAll(contentColumns);
    return new TriggerContentDatabaseTable(connection, keyspace, tableName, columns);
  }

  private TriggerContentDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertContent(
    UUID triggerId, DatabaseRow content
  ) {
    insert(DatabaseRow.of(triggerId).concat(content));
  }

  public void deleteContent(UUID triggerId) {
    delete(DatabaseCell.create(triggerId));
  }

  public CompletableFuture<Boolean> contentExists(UUID triggerId) {
    return exists(DatabaseCell.create(triggerId));
  }

  public CompletableFuture<DatabaseRow> findContent(UUID triggerId) {
    return selectRow(DatabaseCell.create(triggerId));
  }

  public CompletableFuture<List<DatabaseRow>> findContentByCondition(
    String condition
  ) {
    return selectRows(condition);
  }
}
