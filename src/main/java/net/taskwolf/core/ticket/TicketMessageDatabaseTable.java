package net.taskwolf.core.ticket;

import net.taskwolf.core.database.*;
import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class TicketMessageDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "ticket_message";

  public static TicketMessageDatabaseTable create(
          DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("ticket", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("author", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("authorType", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("message", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("time", DatabaseDataType.BIGINT));
    return new TicketMessageDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private TicketMessageDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertTicketMessage(TicketMessage ticket) {
    insertTicketMessage(ticket.id(), ticket.ticket(), ticket.author(),
      ticket.authorType(), ticket.message(), ticket.time());
  }

  public void insertTicketMessage(
    UUID id, UUID ticket, UUID author, TicketMessageAuthorType authorType,
    String message, long time
  ) {
    insert(DatabaseRow.of(id, ticket, author, authorType.toString(), message, time));
  }

  public CompletableFuture<UUID> generateAvailableTicketMessageId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    ticketMessageExists(id).thenApply(exists -> exists ?
      generateAvailableTicketMessageId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Boolean> ticketMessageExists(UUID messageId) {
    return exists(messageId);
  }

  public void deleteTicketMessage(UUID messageId) {
    delete(messageId);
  }

  public CompletableFuture<TicketMessage> findTicketMessage(UUID messageId) {
    return selectRow(messageId).thenApply(TicketMessage::of);
  }
}

