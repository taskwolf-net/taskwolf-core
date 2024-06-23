package net.taskwolf.core.bundle;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;

@RequiredArgsConstructor(staticName = "create")
public final class BundleInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  BundleDatabaseTable provideBundleDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var bundleDatabaseTable = BundleDatabaseTable.create(connection, keyspace);
    bundleDatabaseTable.createIfNotExists();
    return bundleDatabaseTable;
  }
}