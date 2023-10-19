package net.taskwolf.core.organization;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class OrganizationDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "organizations";

  public static OrganizationDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("name", DatabaseDataType.TEXT));
    columns.add(DatabaseListColumn.create("members", DatabaseDataType.UUID));
    return new OrganizationDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private OrganizationDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertOrganization(Organization organization) {
    insertOrganization(organization.id(), organization.name(), organization.members());
  }

  public void insertOrganization(
    UUID id, String name, List<UUID> memberIds
  ) {
    insert(DatabaseRow.of(id, name, memberIds));
  }

  public void deleteOrganization(UUID organizationId) {
    delete(DatabaseCell.create(organizationId));
  }

  public CompletableFuture<Organization> findOrganization(UUID organizationId) {
    return selectRow(DatabaseCell.create(organizationId)).thenApply(Organization::of);
  }

  public CompletableFuture<List<Organization>> findAllOrganization() {
    return selectAllRows().thenApply(rows ->
      rows.stream().map(Organization::of).collect(Collectors.toList()));
  }
}
