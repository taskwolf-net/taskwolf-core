package net.taskwolf.core.trial;

import net.taskwolf.core.database.*;
import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class TrialDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "trial";

  public static TrialDatabaseTable create(
          DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("email", DatabaseDataType.TEXT,
      DatabaseColumn.Type.PRIMARY_KEY));
    return new TrialDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private TrialDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertTrial(String email) {
    insert(DatabaseRow.of(email));
  }

  public void deleteTrial(String email) {
    delete(email);
  }

  public CompletableFuture<Boolean> trialExists(String email) {
    return exists(email);
  }
}
