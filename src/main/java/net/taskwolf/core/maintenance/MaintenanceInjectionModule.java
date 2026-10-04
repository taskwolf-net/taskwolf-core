package net.taskwolf.core.maintenance;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;

@RequiredArgsConstructor(staticName = "create")
public final class MaintenanceInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  MaintenanceDatabaseTable provideMaintenanceDatabaseTable(
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    return MaintenanceDatabaseTable.create(databaseConnection, databaseKeyspace);
  }
}
