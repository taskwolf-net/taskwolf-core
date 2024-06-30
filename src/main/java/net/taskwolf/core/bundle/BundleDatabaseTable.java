package net.taskwolf.core.bundle;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class BundleDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "bundle";

  public static BundleDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("bundleType", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("bundleClass", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("bundlerRuntime", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("price", DatabaseDataType.DOUBLE));
    columns.add(DatabaseColumn.create("expiration", DatabaseDataType.BIGINT));
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
    columns.add(DatabaseColumn.create("deviceAccess", DatabaseDataType.BOOLEAN));
    columns.add(DatabaseColumn.create("accountsAccess", DatabaseDataType.BOOLEAN));
    columns.add(DatabaseColumn.create("accountsNumberLimit", DatabaseDataType.BIGINT));
    return new BundleDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private BundleDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertBundle(Bundle bundle) {
    insert(DatabaseRow.of(bundle.ownerId(), bundle.bundleType().toString(),
      bundle.bundleClass().toString(), bundle.bundleRuntime().toString(),
      bundle.price(), bundle.expiration(), bundle.workflowAccess(),
      bundle.workflowNumberLimit(), bundle.workflowOperationLimit(),
      bundle.workflowTemplateAccess(), bundle.databaseAccess(),
      bundle.databaseNumberLimit(), bundle.databaseDataLimit(),
      bundle.webhookAccess(), bundle.webhookNumberLimit(),
      bundle.organizationAccess(), bundle.organizationMemberLimit(),
      bundle.deviceAccess(), bundle.accountsAccess(),
      bundle.accountsNumberLimit()));
  }

  public void updateBundle(Bundle bundle) {
    update(DatabaseCell.create(bundle.ownerId()), DatabaseRow.of(bundle.ownerId(),
      bundle.bundleType().toString(), bundle.bundleClass().toString(),
      bundle.bundleRuntime().toString(), bundle.price(), bundle.expiration(),
      bundle.workflowAccess(), bundle.workflowNumberLimit(),
      bundle.workflowOperationLimit(), bundle.workflowTemplateAccess(),
      bundle.databaseAccess(), bundle.databaseNumberLimit(),
      bundle.databaseDataLimit(), bundle.webhookAccess(),
      bundle.webhookNumberLimit(), bundle.organizationAccess(),
      bundle.organizationMemberLimit(), bundle.deviceAccess(),
      bundle.accountsAccess(), bundle.accountsNumberLimit()));
  }

  public void deleteBundle(UUID ownerId) {
    delete(DatabaseCell.create(ownerId));
  }

  public CompletableFuture<Boolean> bundleExists(UUID ownerId) {
    return exists(DatabaseCell.create(ownerId));
  }

  public CompletableFuture<Bundle> findBundle(UUID ownerId) {
    return selectRow(DatabaseCell.create(ownerId)).thenApply(Bundle::of);
  }
}

