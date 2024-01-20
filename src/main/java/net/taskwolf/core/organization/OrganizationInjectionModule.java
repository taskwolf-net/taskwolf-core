package net.taskwolf.core.organization;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;

@RequiredArgsConstructor(staticName = "create")
public final class OrganizationInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  OrganizationDatabaseTable provideOrganizationDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var organizationDatabaseTable = OrganizationDatabaseTable.create(connection,
      keyspace);
    organizationDatabaseTable.createIfNotExists();
    return organizationDatabaseTable;
  }
}
