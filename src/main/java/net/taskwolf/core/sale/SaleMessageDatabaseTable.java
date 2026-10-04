package net.taskwolf.core.sale;

import net.taskwolf.core.database.*;
import net.taskwolf.core.database.condition.DatabaseCondition;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class SaleMessageDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "sale_message";

  public static SaleMessageDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("publicId", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("saleId", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("sender", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("senderType", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("content", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("time", DatabaseDataType.BIGINT));
    return new SaleMessageDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private SaleMessageDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public CompletableFuture<Void> insertSaleMessage(
    SaleMessage saleMessage
  ) {
    return insertSaleMessage(saleMessage.id(), saleMessage.publicId(),
      saleMessage.saleId(), saleMessage.sender(), saleMessage.senderType(),
      saleMessage.content(), saleMessage.time());
  }

  public CompletableFuture<Void> insertSaleMessage(
    UUID id, String publicId, UUID saleId, String sender,
    SaleMessageSenderType senderType, String content, long time
  ) {
    return insert(DatabaseRow.of(id, publicId, saleId, sender,
      senderType.toString(), content, time));
  }

  public CompletableFuture<UUID> generateAvailableMessageId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    saleMessageExists(id).thenApply(exists -> exists ?
      generateAvailableMessageId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Void> deleteSaleMessage(UUID id) {
    return delete(id);
  }

  public CompletableFuture<Boolean> saleMessageExists(UUID id) {
    return exists(id);
  }

  public CompletableFuture<Boolean> saleMessageExists(String publicId) {
    return exists(DatabaseCondition.of("publicId", publicId));
  }

  public CompletableFuture<SaleMessage> findSaleMessage(UUID id) {
    return selectRow(id).thenApply(SaleMessage::of);
  }

  public CompletableFuture<SaleMessage> findSaleMessage(String publicId) {
    return selectRow(DatabaseCondition.of("publicId", publicId))
      .thenApply(SaleMessage::of);
  }

  public CompletableFuture<List<SaleMessage>> findMessagesOfSale(
    UUID saleId
  ) {
    return selectRows(DatabaseCondition.of("saleId", saleId))
      .thenApply(rows -> rows.stream().map(SaleMessage::of).toList());
  }
}
