package com.dulno.core;

import com.dulno.core.database.*;
import com.google.common.collect.Lists;
import com.dulno.core.database.*;
import com.dulno.core.database.transformation.DatabaseTransformation;

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
    var table = new TestDatabaseTable(connection, keyspace, TABLE_NAME, columns,
      TestDatabaseTableTransformation.create());
    table.createIfNotExists();
    //table.createIndexIfNotExists("email");
    //table.createMaterializedViewIfNotExists("email_view", "email");
    return table;
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
