package com.dulno.core.ticket;

import com.dulno.core.database.*;
import com.dulno.core.database.condition.DatabaseComparison;
import com.dulno.core.database.condition.DatabaseCondition;
import com.dulno.core.database.paging.DatabaseDirection;
import com.dulno.core.database.paging.DatabaseOrder;
import com.dulno.core.database.paging.DatabasePage;
import com.dulno.core.question.Question;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class TicketDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "ticket";

  public static TicketDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("creator", DatabaseDataType.UUID,
      DatabaseColumn.Type.PARTITION_KEY));
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.CLUSTERING_KEY));
    columns.add(DatabaseColumn.create("title", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("type", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("status", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("expirationTime", DatabaseDataType.BIGINT));
    columns.add(DatabaseListColumn.create("messages", DatabaseDataType.UUID));
    var table = new TicketDatabaseTable(connection, keyspace, TABLE_NAME, columns);
    table.createIfNotExists();
    table.createIndexIfNotExists("id");
    table.createIndexIfNotExists("status");
    table.createIndexIfNotExists("title",
      "'org.apache.cassandra.index.sasi.SASIIndex' WITH OPTIONS = " +
        "{'mode': 'CONTAINS', 'analyzer_class': " +
        "'org.apache.cassandra.index.sasi.analyzer.NonTokenizingAnalyzer', " +
        "'case_sensitive': 'false'}");
    table.initializeViews();
    return table;
  }

  private DatabaseTable titleView;
  private DatabaseTable typeView;
  private DatabaseTable statusView;

  private TicketDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  private void initializeViews() {
    titleView = createMaterializedViewIfNotExists("title_view", "title");
    typeView = createMaterializedViewIfNotExists("type_view", "type");
    statusView = createMaterializedViewIfNotExists("status_view", "status");
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
    insert(DatabaseRow.of(creator, id, title, type, status, expirationTime,
      messageIds));
  }

  public void addTicketMessage(UUID ticketId, UUID messageId) {
    findTicket(ticketId).thenAccept(ticket ->
      addTicketMessage(ticket, messageId));
  }

  private void addTicketMessage(Ticket ticket, UUID messageId) {
    ticket.addMessage(messageId);
    updateTicket(ticket);
  }

  public void removeTicketMessage(UUID ticketId, UUID messageId) {
    findTicket(ticketId).thenAccept(ticket ->
      removeTicketMessage(ticket, messageId));
  }

  private void removeTicketMessage(Ticket ticket, UUID messageId) {
    ticket.removeMessage(messageId);
    updateTicket(ticket);
  }

  public void updateTicketStatus(UUID ticketId, Ticket.Status title) {
    findTicket(ticketId).thenAccept(ticket ->
      updateTicketStatus(ticket, title));
  }

  private void updateTicketStatus(Ticket ticket, Ticket.Status title) {
    ticket.updateStatus(title);
    updateTicket(ticket);
  }

  public void renameTicket(UUID ticketId, String title) {
    findTicket(ticketId).thenAccept(ticket ->
      renameTicket(ticket, title));
  }

  private void renameTicket(Ticket ticket, String title) {
    ticket.rename(title);
    updateTicket(ticket);
  }

  public void updateTicket(Ticket ticket) {
    update(DatabaseCondition.of("creator", ticket.creator(), "id", ticket.id()),
      DatabaseRow.of(ticket.creator(), ticket.id(), ticket.title(),
        ticket.type().toString(), ticket.status().toString(),
        ticket.expirationTime(), ticket.messages()));
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
    return exists(DatabaseCondition.of("id", ticketId));
  }

  public void deleteTicket(UUID ticketId) {
    findTicket(ticketId).thenAccept(ticket ->
      delete(DatabaseCondition.of("creator", ticket.creator(), "id", ticket.id())));
  }

  public CompletableFuture<Ticket> findTicket(UUID ticketId) {
    return selectRow(DatabaseCondition.of("id", ticketId))
      .thenApply(row -> Ticket.of(row, this));
  }

  private static final int PAGE_SIZE = 5;

  public CompletableFuture<DatabasePage<Ticket>> findTicketsOfCreator(
    UUID creatorId, int targetPage, String sortingColumn, DatabaseOrder sortingOrder,
    String search, String type, String status
  ) {
    if (!search.isEmpty()) {
      var condition = DatabaseCondition.of(DatabaseComparison.create("creator", creatorId),
        DatabaseComparison.create("title", "%" + search + "%", DatabaseComparison.Type.LIKE));
      return selectRows(condition, PAGE_SIZE)
        .thenApply(rows -> createTicketPage(DatabasePage.create(rows, "", 1), this));
    }
    var view = findTargetView(sortingColumn);
    return view.selectPage(creatorId, createTicketConditions(type, status),
        sortingOrder, PAGE_SIZE, targetPage)
      .thenApply(page -> createTicketPage(page, view));
  }

  public CompletableFuture<DatabasePage<Ticket>> findTicketsOfCreator(
    UUID creatorId, String pageState, DatabaseDirection startingPoint,
    DatabaseDirection direction, String sortingColumn, DatabaseOrder sortingOrder,
    String type, String status
  ) {
    var view = findTargetView(sortingColumn);
    return view.shiftPage(creatorId, createTicketConditions(type, status),
        sortingOrder, PAGE_SIZE, pageState, startingPoint, direction)
      .thenApply(page -> createTicketPage(page, view));
  }

  private DatabaseTable findTargetView(String sortingColumn) {
    if (sortingColumn.equals("title")) {
      return titleView;
    } else if (sortingColumn.equals("type")) {
      return typeView;
    } else if (sortingColumn.equals("status")) {
      return statusView;
    }
    return null;
  }

  private DatabaseCondition createTicketConditions(
    String type, String status
  ) {
    var comparisons = Lists.<DatabaseComparison>newArrayList();
    if (type != null) {
      comparisons.add(DatabaseComparison.create("type", type));
    }
    if (status != null) {
      comparisons.add(DatabaseComparison.create("status", status));
    }
    return DatabaseCondition.create(comparisons);
  }

  private DatabasePage<Ticket> createTicketPage(
    DatabasePage<DatabaseRow> page, DatabaseTable table
  ) {
    return DatabasePage.create(
      page.content().stream().map(row -> Ticket.of(row, table)).toList(),
      page.pageState(), page.pageNumber());
  }

  public CompletableFuture<List<Ticket>> findAllTicketsOfCreator(UUID creatorId) {
    return selectRows(DatabaseCondition.of("creator", creatorId))
      .thenApply(rows -> rows.stream().map(row -> Ticket.of(row, this)).toList());
  }

  public CompletableFuture<Long> findTicketCount(UUID creatorId) {
    return count(DatabaseCondition.of(DatabaseCondition.Filtering.ALLOWED,
      DatabaseComparison.create("creator", creatorId),
      DatabaseComparison.create("status", Ticket.Status.OPEN.toString())));
  }

  public CompletableFuture<List<Ticket>> findOpenTickets() {
    return selectRows(DatabaseCondition.of("status", Question.Status.OPEN.toString()))
      .thenApply(rows -> rows.stream().map(row -> Ticket.of(row, this)).toList());
  }
}
