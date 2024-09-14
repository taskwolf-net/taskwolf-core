package com.dulno.core.mail;

import com.dulno.core.database.*;
import com.google.common.collect.Lists;
import com.dulno.core.database.*;
import com.dulno.core.database.condition.DatabaseCondition;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class OutgoingMailDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "outgoing_mail";

  public static OutgoingMailDatabaseTable create(
          DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("receiver", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("sender", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("time", DatabaseDataType.BIGINT));
    columns.add(DatabaseColumn.create("title", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("content", DatabaseDataType.BLOB));
    var outgoingMailDatabaseTable =  new OutgoingMailDatabaseTable(connection,
      keyspace, TABLE_NAME, columns);
    outgoingMailDatabaseTable.createIfNotExists();
    outgoingMailDatabaseTable.createIndexIfNotExists("receiver");
    return outgoingMailDatabaseTable;
  }

  private OutgoingMailDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public CompletableFuture<Void> insertOutgoingMail(OutgoingMail outgoingMail) {
    return insertOutgoingMail(outgoingMail.id(), outgoingMail.receiver(),
      outgoingMail.sender(), outgoingMail.time(), outgoingMail.title(),
      outgoingMail.content());
  }

  public CompletableFuture<Void> insertOutgoingMail(
    UUID id, String receiver, String sender, long time, String title,
    byte[] content
  ) {
    return insert(DatabaseRow.of(id, receiver, sender, time, title,
      ByteBuffer.wrap(content)));
  }

  public CompletableFuture<UUID> generateAvailableOutgoingMailId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    outgoingMailExists(id).thenApply(exists -> exists ?
      generateAvailableOutgoingMailId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Void> deleteOutgoingMail(UUID id) {
    return delete(id);
  }

  public CompletableFuture<Boolean> outgoingMailExists(UUID id) {
    return exists(id);
  }

  public CompletableFuture<OutgoingMail> findOutgoingMail(UUID id) {
    return selectRow(id).thenApply(OutgoingMail::of);
  }

  public CompletableFuture<List<OutgoingMail>> findOutgoingMailsByReceiver(
    String receiver
  ) {
    return selectRows(DatabaseCondition.of("receiver", receiver))
      .thenApply(rows -> rows.stream().map(OutgoingMail::of).toList());
  }
}