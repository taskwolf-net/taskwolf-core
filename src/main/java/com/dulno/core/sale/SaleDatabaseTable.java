package com.dulno.core.sale;

import com.dulno.core.database.*;
import com.dulno.core.database.condition.DatabaseComparison;
import com.dulno.core.question.Question;
import com.dulno.core.ticket.Ticket;
import com.google.common.collect.Lists;
import com.dulno.core.database.condition.DatabaseCondition;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class SaleDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "sale";

  public static SaleDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("sender", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("firstName", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("lastName", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("phoneNumber", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("country", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("companyName", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("companySize", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("companyRole", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("title", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("status", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("expirationTime", DatabaseDataType.BIGINT));
    return new SaleDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private SaleDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public CompletableFuture<Void> insertSale(Sale sale) {
    return insertSale(sale.id(), sale.sender(), sale.firstName(),
      sale.lastName(), sale.phoneNumber(), sale.country(), sale.companyName(),
      sale.companySize(), sale.companyRole(), sale.title(),
      sale.status().toString(), sale.expirationTime());
  }

  public CompletableFuture<Void> insertSale(
    UUID id, String sender, String firstName, String lastName,
    String phoneNumber, String country, String companyName, String companySize,
    String companyRole, String title, String status, long expirationTime
  ) {
    return insert(DatabaseRow.of(id, sender, firstName, lastName, phoneNumber,
      country, companyName, companySize, companyRole, title, status,
      expirationTime));
  }

  public CompletableFuture<Void> updateSaleStatus(UUID id, Sale.Status status) {
    var futureResponse = new CompletableFuture<Void>();
    findSale(id).thenAccept(sale -> updateSaleStatus(sale, status)
      .thenAccept(futureResponse::complete));
    return futureResponse;
  }

  private CompletableFuture<Void> updateSaleStatus(Sale sale, Sale.Status status) {
    sale.updateStatus(status);
    return updateSale(sale);
  }

  public CompletableFuture<Void> resetSaleExpirationTime(UUID id) {
    var futureResponse = new CompletableFuture<Void>();
    findSale(id).thenAccept(sale -> resetSaleExpirationTime(sale)
      .thenAccept(futureResponse::complete));
    return futureResponse;
  }

  private CompletableFuture<Void> resetSaleExpirationTime(Sale sale) {
    sale.resetExpirationTime();
    return updateSale(sale);
  }

  public CompletableFuture<Void> disableSaleExpirationTime(UUID id) {
    var futureResponse = new CompletableFuture<Void>();
    findSale(id).thenAccept(sale -> disableSaleExpirationTime(sale)
      .thenAccept(futureResponse::complete));
    return futureResponse;
  }

  private CompletableFuture<Void> disableSaleExpirationTime(Sale sale) {
    sale.disableExpirationTime();
    return updateSale(sale);
  }

  public CompletableFuture<Void> updateSale(Sale sale) {
    return update(sale.id(), DatabaseRow.of(sale.id(), sale.sender(),
      sale.firstName(), sale.lastName(), sale.phoneNumber(), sale.country(),
      sale.companyName(), sale.companySize(), sale.companyRole(), sale.title(),
      sale.status().toString(), sale.expirationTime()));
  }

  public CompletableFuture<UUID> generateAvailableSaleId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    saleExists(id).thenApply(exists -> exists ?
      generateAvailableSaleId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Void> deleteSale(UUID id) {
    return delete(id);
  }

  public CompletableFuture<Boolean> saleExists(UUID id) {
    return exists(id);
  }

  public CompletableFuture<Sale> findSale(UUID id) {
    return selectRow(id).thenApply(Sale::of);
  }

  public CompletableFuture<List<Sale>> findSalesBySender(String sender) {
    return selectRows(DatabaseCondition.of("sender", sender))
      .thenApply(rows -> rows.stream().map(Sale::of).toList());
  }

  public CompletableFuture<List<Sale>> findOpenSales() {
    return selectRows(DatabaseCondition.of("status", Question.Status.OPEN.toString()))
      .thenApply(rows -> rows.stream().map(Sale::of).collect(Collectors.toList()));
  }

  public CompletableFuture<Long> countPendingSales() {
    return count(DatabaseCondition.of(DatabaseCondition.Filtering.ALLOWED,
      DatabaseComparison.create("expirationTime", -1L),
      DatabaseComparison.create("status", Ticket.Status.OPEN.toString())));
  }
}
