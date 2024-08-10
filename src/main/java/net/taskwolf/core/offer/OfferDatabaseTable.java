package net.taskwolf.core.offer;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class OfferDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "offer";

  public static OfferDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("target", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("priceId", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("offerStatus", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("bundleType", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("bundleClass", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("bundlerRuntime", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("price", DatabaseDataType.DOUBLE));
    columns.add(DatabaseColumn.create("workflowAccess", DatabaseDataType.BOOLEAN));
    columns.add(DatabaseColumn.create("workflowNumberLimit", DatabaseDataType.BIGINT));
    columns.add(DatabaseColumn.create("workflowOperationLimit", DatabaseDataType.BIGINT));
    columns.add(DatabaseColumn.create("workflowTemplateAccess", DatabaseDataType.BOOLEAN));
    columns.add(DatabaseColumn.create("databaseAccess", DatabaseDataType.BOOLEAN));
    columns.add(DatabaseColumn.create("databaseNumberLimit", DatabaseDataType.BIGINT));
    columns.add(DatabaseColumn.create("databaseDataLimit", DatabaseDataType.DOUBLE));
    columns.add(DatabaseColumn.create("webhookAccess", DatabaseDataType.BOOLEAN));
    columns.add(DatabaseColumn.create("webhookNumberLimit", DatabaseDataType.BIGINT));
    columns.add(DatabaseColumn.create("organizationAccess", DatabaseDataType.BOOLEAN));
    columns.add(DatabaseColumn.create("organizationMemberLimit", DatabaseDataType.BIGINT));
    columns.add(DatabaseColumn.create("organizationTeamLimit", DatabaseDataType.BIGINT));
    columns.add(DatabaseColumn.create("deviceAccess", DatabaseDataType.BOOLEAN));
    columns.add(DatabaseColumn.create("accountsAccess", DatabaseDataType.BOOLEAN));
    columns.add(DatabaseColumn.create("accountsNumberLimit", DatabaseDataType.BIGINT));
    return new OfferDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private OfferDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public CompletableFuture<Void> insertOffer(Offer offer) {
    return insert(DatabaseRow.of(offer.id(), offer.targetId(), offer.priceId(),
      offer.offerStatus().toString(), offer.bundleType().toString(),
      offer.bundleClass().toString(), offer.bundleRuntime().toString(),
      offer.price(), offer.workflowAccess(), offer.workflowNumberLimit(),
      offer.workflowOperationLimit(), offer.workflowTemplateAccess(),
      offer.databaseAccess(), offer.databaseNumberLimit(),
      offer.databaseDataLimit(), offer.webhookAccess(),
      offer.webhookNumberLimit(), offer.organizationAccess(),
      offer.organizationMemberLimit(), offer.organizationTeamLimit(),
      offer.deviceAccess(), offer.accountsAccess(), offer.accountsNumberLimit()));
  }

  public void updateOfferStatus(Offer offer, OfferStatus newStatus) {
    offer.updateStatus(newStatus);
    updateOffer(offer);
  }

  public CompletableFuture<Void> updateOffer(Offer offer) {
    return update(DatabaseCell.create(offer.id()), DatabaseRow.of(offer.id(),
      offer.targetId(), offer.priceId(), offer.offerStatus().toString(),
      offer.bundleType().toString(), offer.bundleClass().toString(),
      offer.bundleRuntime().toString(), offer.price(), offer.workflowAccess(),
      offer.workflowNumberLimit(), offer.workflowOperationLimit(),
      offer.workflowTemplateAccess(), offer.databaseAccess(),
      offer.databaseNumberLimit(), offer.databaseDataLimit(), offer.webhookAccess(),
      offer.webhookNumberLimit(), offer.organizationAccess(),
      offer.organizationMemberLimit(), offer.organizationTeamLimit(),
      offer.deviceAccess(), offer.accountsAccess(), offer.accountsNumberLimit()));
  }

  public CompletableFuture<UUID> generateAvailableOfferId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    offerExists(id).thenApply(exists -> exists ?
      generateAvailableOfferId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Void> deleteOffer(UUID offerId) {
    return delete(DatabaseCell.create(offerId));
  }

  public CompletableFuture<Boolean> offerExists(UUID offerId) {
    return exists(DatabaseCell.create(offerId));
  }

  public CompletableFuture<Offer> findOffer(UUID offerId) {
    return selectRow(DatabaseCell.create(offerId)).thenApply(Offer::of);
  }

  public CompletableFuture<Offer> findOffersByPriceId(String priceId) {
    return selectRow("priceId='" + priceId + "' ALLOW FILTERING")
      .thenApply(Offer::of);
  }

  public CompletableFuture<List<Offer>> findOffersByTarget(UUID targetId) {
    return selectRows("target=" + targetId + " ALLOW FILTERING")
      .thenApply(rows -> rows.stream().map(Offer::of).toList());
  }
}