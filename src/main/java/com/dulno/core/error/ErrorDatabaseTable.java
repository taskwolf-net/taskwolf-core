package com.dulno.core.error;

import com.dulno.core.database.*;
import com.dulno.core.database.condition.DatabaseCondition;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class ErrorDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "error";

  public static ErrorDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("title", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("trace", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("origin", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("pod", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("controllerName", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("controllerType", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("node", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("time", DatabaseDataType.BIGINT));
    columns.add(DatabaseColumn.create("state", DatabaseDataType.TEXT));
    return new ErrorDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private ErrorDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public CompletableFuture<Void> insertError(DulnoError error) {
    return insert(DatabaseRow.of(error.id(), error.title(),
      error.trace(), error.origin(), error.pod(), error.podControllerName(),
      error.podControllerType(), error.node(), error.time(),
      error.state().toString()));
  }

  public void updateErrorState(DulnoError error, DulnoErrorState state) {
    error.updateState(state);
    updateError(error);
  }

  public CompletableFuture<Void> updateError(DulnoError error) {
    return update(error.id(), DatabaseRow.of(error.id(), error.title(),
      error.trace(), error.origin(), error.pod(), error.podControllerName(),
      error.podControllerType(), error.node(), error.time(),
      error.state().toString()));
  }

  public CompletableFuture<UUID> generateAvailableErrorId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    errorExists(id).thenApply(exists -> exists ?
      generateAvailableErrorId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Void> deleteError(UUID errorId) {
    return delete(errorId);
  }

  public CompletableFuture<Boolean> errorExists(UUID errorId) {
    return exists(errorId);
  }

  public CompletableFuture<DulnoError> findError(UUID errorId) {
    return selectRow(errorId).thenApply(DulnoError::of);
  }

  public CompletableFuture<List<DulnoError>> findErrorsByState(
    DulnoErrorState state
  ) {
    return selectRows(DatabaseCondition.of("state", state.toString()))
      .thenApply(rows -> rows.stream().map(DulnoError::of).toList());
  }
}