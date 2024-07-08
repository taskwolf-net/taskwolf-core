package net.taskwolf.core.stripe;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class TerminationDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "stripe_termination";

  public static TerminationDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("target", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    return new TerminationDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private TerminationDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public CompletableFuture<Void> insertTermination(UUID target) {
    return insert(DatabaseRow.of(target));
  }

  public CompletableFuture<Void> deleteTermination(UUID target) {
    return delete(DatabaseCell.create(target));
  }

  public CompletableFuture<Boolean> terminationExists(UUID target) {
    return exists(DatabaseCell.create(target));
  }
}
