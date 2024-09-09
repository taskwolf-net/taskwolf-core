package net.taskwolf.core;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;
import net.taskwolf.core.database.transformation.DatabaseTransformation;

import java.util.List;

public final class TestDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "test";

  public static TestDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("name", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("email", DatabaseDataType.TEXT));
    return new TestDatabaseTable(connection, keyspace, TABLE_NAME, columns,
      TestDatabaseTableTransformation.create());
  }

  private TestDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  private TestDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns, DatabaseTransformation transformation
  ) {
    super(connection, keyspace, name, columns, transformation);
  }
}
