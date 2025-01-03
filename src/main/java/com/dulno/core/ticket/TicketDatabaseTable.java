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
import java.util.stream.Collectors;

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
    columns.add(DatabaseColumn.create("expirationTime", DatabaseDataType.BIGINT,
      DatabaseColumn.Type.CLUSTERING_KEY));
    columns.add(DatabaseListColumn.create("messages", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("lastMessageSeen", DatabaseDataType.BOOLEAN));
    var table = new TicketDatabaseTable(connection, keyspace, TABLE_NAME, columns);
    table.createIfNotExists();
    table.createIndexIfNotExists("status");
    table.createIndexIfNotExists("lastMessageSeen");
    table.createIndexIfNotExists("title",
      "'org.apache.cassandra.index.sasi.SASIIndex' WITH OPTIONS = " +
        "{'mode': 'CONTAINS', 'analyzer_class': " +
        "'org.apache.cassandra.index.sasi.analyzer.NonTokenizingAnalyzer', " +
        "'case_sensitive': 'false'}");
    table.initializeViews();
    return table;
  }

  private DatabaseTable idView;
  private DatabaseTable titleView;
  private DatabaseTable typeView;
  private DatabaseTable statusView;
  private DatabaseTable statusExpirationView;

  private TicketDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  private void initializeViews() {
    idView = createMaterializedViewIfNotExists("id_view", "id",
      DatabaseColumn.Type.PARTITION_KEY);
    titleView = createMaterializedViewIfNotExists("title_view", "title");
    typeView = createMaterializedViewIfNotExists("type_view", "type");
    statusView = createMaterializedViewIfNotExists("status_view", "status");
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("status", DatabaseDataType.TEXT,
      DatabaseColumn.Type.PARTITION_KEY));
    columns.add(DatabaseColumn.create("expirationTime", DatabaseDataType.BIGINT,
      DatabaseColumn.Type.CLUSTERING_KEY));
    columns.add(DatabaseColumn.create("creator", DatabaseDataType.UUID,
      DatabaseColumn.Type.CLUSTERING_KEY));
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.CLUSTERING_KEY));
    statusExpirationView = createMaterializedViewIfNotExists(
      "status_expiration_view", columns);
  }

  public CompletableFuture<Void> insertTicket(Ticket ticket) {
    return insertTicket(ticket.id(), ticket.creator(), ticket.title(),
      ticket.type().toString(), ticket.status().toString(),
      ticket.expirationTime(), ticket.messages(), ticket.lastMessageSeen());
  }

  public CompletableFuture<Void> insertTicket(
    UUID id, UUID creator, String title, String type, String status,
    long expirationTime, List<UUID> messageIds, boolean lastMessageSeen
  ) {
    return insert(DatabaseRow.of(creator, id, title, type, status, expirationTime,
      messageIds, lastMessageSeen));
  }

  public CompletableFuture<Void> addTicketMessage(UUID ticketId, UUID messageId) {
    return findTicket(ticketId).thenCompose(ticket ->
      addTicketMessage(ticket, messageId));
  }

  private CompletableFuture<Void> addTicketMessage(Ticket ticket, UUID messageId) {
    ticket.addMessage(messageId);
    return updateTicket(ticket);
  }

  public CompletableFuture<Void> removeTicketMessage(UUID ticketId, UUID messageId) {
    return findTicket(ticketId).thenCompose(ticket ->
      removeTicketMessage(ticket, messageId));
  }

  private CompletableFuture<Void> removeTicketMessage(Ticket ticket, UUID messageId) {
    ticket.removeMessage(messageId);
    return updateTicket(ticket);
  }

  public CompletableFuture<Void> updateTicketStatus(
    UUID ticketId, Ticket.Status title
  ) {
    return findTicket(ticketId).thenCompose(ticket ->
      updateTicketStatus(ticket, title));
  }

  private CompletableFuture<Void> updateTicketStatus(
    Ticket ticket, Ticket.Status title
  ) {
    ticket.updateStatus(title);
    return updateTicket(ticket);
  }

  public CompletableFuture<Void> renameTicket(UUID ticketId, String title) {
    return findTicket(ticketId).thenCompose(ticket ->
      renameTicket(ticket, title));
  }

  private CompletableFuture<Void> renameTicket(Ticket ticket, String title) {
    ticket.rename(title);
    return updateTicket(ticket);
  }

  public CompletableFuture<Void> updateTicketLastMessageSeen(
    UUID ticketId, boolean lastMessageSeen
  ) {
    return findTicket(ticketId).thenCompose(ticket ->
      updateTicketLastMessageSeen(ticket, lastMessageSeen));
  }

  private CompletableFuture<Void> updateTicketLastMessageSeen(
    Ticket ticket, boolean lastMessageSeen
  ) {
    ticket.updateLastMessageSeen(lastMessageSeen);
    return updateTicket(ticket);
  }

  private CompletableFuture<Void> updateTicket(Ticket ticket) {
    var condition = DatabaseCondition.of("creator", ticket.creator(),
      "id", ticket.id(), "expirationTime", ticket.expirationTime());
    return update(condition, DatabaseRow.of(ticket.creator(), ticket.id(),
      ticket.title(), ticket.type().toString(), ticket.status().toString(),
      ticket.expirationTime(), ticket.messages(), ticket.lastMessageSeen()));
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
    return idView.exists(DatabaseCondition.of("id", ticketId));
  }

  public CompletableFuture<Void> deleteTicket(UUID ticketId) {
    return findTicket(ticketId).thenCompose(ticket ->
      delete(DatabaseCondition.of("creator", ticket.creator(), "id", ticket.id(),
        "expirationTime", ticket.expirationTime())));
  }

  public CompletableFuture<Ticket> findTicket(UUID ticketId) {
    return idView.selectRow(DatabaseCondition.of("id", ticketId))
      .thenApply(row -> Ticket.of(row, idView));
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

  public CompletableFuture<List<Ticket>> findAllOpenTicketsOfCreator(UUID creatorId) {
    var condition = DatabaseCondition.of(
      DatabaseComparison.create("creator", creatorId),
      DatabaseComparison.create("status", Ticket.Status.OPEN.toString()));
    return selectRows(condition)
      .thenApply(rows -> rows.stream().map(row -> Ticket.of(row, this)).toList());
  }

  public CompletableFuture<Long> findTicketCount(UUID creatorId) {
    return count(DatabaseCondition.of(
      DatabaseComparison.create("creator", creatorId),
      DatabaseComparison.create("status", Ticket.Status.OPEN.toString())));
  }

  public CompletableFuture<Boolean> hasUnseenTickets(UUID creatorId) {
    return exists(DatabaseCondition.of(
      DatabaseComparison.create("creator", creatorId),
      DatabaseComparison.create("lastMessageSeen", false)));
  }

  public CompletableFuture<List<Ticket>> findOpenTickets() {
    return selectRows(DatabaseCondition.of("status", Question.Status.OPEN.toString()))
      .thenApply(rows -> rows.stream().map(row -> Ticket.of(row, this))
        .collect(Collectors.toList()));
  }

  public CompletableFuture<Long> countPendingTickets() {
    return statusExpirationView.count(DatabaseCondition.of(
      DatabaseComparison.create("expirationTime", -1L),
      DatabaseComparison.create("status", Ticket.Status.OPEN.toString())));
  }
}
