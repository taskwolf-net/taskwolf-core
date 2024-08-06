package net.taskwolf.core.sale;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

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
    columns.add(DatabaseColumn.create("requestMessage", DatabaseDataType.UUID,
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
    columns.add(DatabaseColumn.create("expirationTime", DatabaseDataType.BIGINT));
    columns.add(DatabaseListColumn.create("conversationMessages", DatabaseDataType.UUID));
    return new SaleDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private SaleDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public CompletableFuture<Void> insertSale(Sale sale) {
    return insertSale(sale.requestMessage(), sale.sender(), sale.firstName(),
      sale.lastName(), sale.phoneNumber(), sale.country(), sale.companyName(),
      sale.companySize(), sale.companyRole(), sale.title(), sale.expirationTime(),
      sale.conversationMessages());
  }

  public CompletableFuture<Void> insertSale(
    UUID id, String sender, String firstName, String lastName,
    String phoneNumber, String country, String companyName, String companySize,
    String companyRole, String title, long expirationTime,
    List<UUID> conversationMessages
  ) {
    return insert(DatabaseRow.of(id, sender, firstName, lastName, phoneNumber,
      country, companyName, companySize, companyRole, title, expirationTime,
      conversationMessages));
  }

  public CompletableFuture<UUID> generateAvailableSaleId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    saleExists(id).thenApply(exists -> exists ?
      generateAvailableSaleId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Void> addSaleMessage(
    UUID id, UUID conversationMessage
  ) {
    var futureResponse = new CompletableFuture<Void>();
    findSale(id)
      .thenAccept(sale -> addSaleMessage(sale, conversationMessage)
      .thenAccept(futureResponse::complete));
    return futureResponse;
  }

  private CompletableFuture<Void> addSaleMessage(
    Sale sale, UUID conversationMessage
  ) {
    sale.addConversationMessage(conversationMessage);
    return updateSale(sale);
  }

  public CompletableFuture<Void> removeSaleMessage(
    UUID id, UUID conversationMessage
  ) {
    var futureResponse = new CompletableFuture<Void>();
    findSale(id)
      .thenAccept(sale -> removeSaleMessage(sale, conversationMessage)
      .thenAccept(futureResponse::complete));
    return futureResponse;
  }

  private CompletableFuture<Void> removeSaleMessage(
    Sale sale, UUID conversationMessage
  ) {
    sale.removeConversationMessage(conversationMessage);
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
    return update(DatabaseCell.create(sale.requestMessage()),
      DatabaseRow.of(sale.requestMessage(), sale.sender(),
        sale.title(), sale.expirationTime(),
        sale.conversationMessages()));
  }

  public CompletableFuture<Void> deleteSale(UUID id) {
    return delete(DatabaseCell.create(id));
  }

  public CompletableFuture<Boolean> saleExists(UUID id) {
    return exists(DatabaseCell.create(id));
  }

  public CompletableFuture<Sale> findSale(UUID id) {
    return selectRow(DatabaseCell.create(id)).thenApply(Sale::of);
  }

  public CompletableFuture<List<Sale>> findSalesBySender(String sender) {
    return selectRows("sender='" + sender + "' ALLOW FILTERING")
      .thenApply(rows -> rows.stream().map(Sale::of).toList());
  }

  public CompletableFuture<List<Sale>> findAllSales() {
    return selectAllRows().thenApply(rows ->
      rows.stream().map(Sale::of).collect(Collectors.toList()));
  }
}
