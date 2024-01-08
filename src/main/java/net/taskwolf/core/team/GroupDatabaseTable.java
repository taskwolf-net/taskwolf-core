package net.taskwolf.core.team;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class GroupDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "group";

  public static GroupDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("name", DatabaseDataType.TEXT,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("permission", DatabaseDataType.INT));
    return new GroupDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private GroupDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertGroup(Group group) {
    insertGroup(group.name(), group.permission());
  }

  public void insertGroup(String name, int permission) {
    insert(DatabaseRow.of(name, permission));
  }

  public void deleteGroup(String name) {
    delete(DatabaseCell.create(name));
  }

  public CompletableFuture<Boolean> groupExists(String name) {
    return exists(DatabaseCell.create(name));
  }

  public CompletableFuture<Group> findGroup(String name) {
    return selectRow(DatabaseCell.create(name)).thenApply(Group::of);
  }
}
