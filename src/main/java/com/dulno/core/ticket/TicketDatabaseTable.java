package com.dulno.core.ticket;

import com.dulno.core.database.*;
import com.dulno.core.database.condition.DatabaseCondition;
import com.google.common.collect.Lists;
import com.dulno.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class TicketDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "ticket";

  public static TicketDatabaseTable create(
          DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("creator", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("title", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("type", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("status", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("expirationTime", DatabaseDataType.BIGINT));
    columns.add(DatabaseListColumn.create("messages", DatabaseDataType.UUID));
    return new TicketDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private TicketDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertTicket(Ticket ticket) {
    insertTicket(ticket.id(), ticket.creator(), ticket.title(),
      ticket.type().toString(), ticket.status().toString(),
      ticket.expirationTime(), ticket.messages());
  }

  public void insertTicket(
    UUID id, UUID creator, String title, String type, String status,
    long expirationTime, List<UUID> messageIds
  ) {
    insert(DatabaseRow.of(id, creator, title, type, status, expirationTime,
      messageIds));
  }

  public void addTicketMessage(UUID ticketId, UUID messageId) {
    findTicket(ticketId).thenAccept(organization ->
      addTicketMessage(organization, messageId));
  }

  private void addTicketMessage(Ticket ticket, UUID messageId) {
    ticket.addMessage(messageId);
    updateTicket(ticket);
  }

  public void removeTicketMessage(UUID ticketId, UUID messageId) {
    findTicket(ticketId).thenAccept(organization ->
      removeTicketMessage(organization, messageId));
  }

  private void removeTicketMessage(Ticket ticket, UUID messageId) {
    ticket.removeMessage(messageId);
    updateTicket(ticket);
  }

  public void renameTicket(UUID ticketId, String title) {
    findTicket(ticketId).thenAccept(organization ->
      renameTicket(organization, title));
  }

  private void renameTicket(Ticket ticket, String title) {
    ticket.rename(title);
    updateTicket(ticket);
  }

  public void updateTicket(Ticket ticket) {
    update(ticket.id(), DatabaseRow.of(ticket.id(),
      ticket.creator(), ticket.title(), ticket.type().toString(),
      ticket.status().toString(), ticket.expirationTime(), ticket.messages()));
  }

  public CompletableFuture<UUID> generateAvailableTicketId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    ticketExists(id).thenApply(exists -> exists ?
      generateAvailableTicketId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Boolean> ticketExists(UUID ticketId) {
    return exists(ticketId);
  }

  public void deleteTicket(UUID ticketId) {
    delete(ticketId);
  }

  public CompletableFuture<Ticket> findTicket(UUID ticketId) {
    return selectRow(ticketId).thenApply(Ticket::of);
  }

  public CompletableFuture<List<Ticket>> findTicketsByCreator(UUID creatorId) {
    return selectRows(DatabaseCondition.of("creator", creatorId)).thenApply(rows ->
      rows.stream().map(Ticket::of).toList());
  }

  public CompletableFuture<List<Ticket>> findAllTickets() {
    return selectAllRows().thenApply(rows ->
      rows.stream().map(Ticket::of).toList());
  }
}
