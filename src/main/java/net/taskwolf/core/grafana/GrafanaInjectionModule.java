package net.taskwolf.core.grafana;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;

@RequiredArgsConstructor(staticName = "create")
public final class GrafanaInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  GrafanaConfiguration provideGrafanaConfiguration() throws Exception {
    return GrafanaConfiguration.createAndLoad();
  }

  @Provides
  @Singleton
  GrafanaDatabaseTable provideGrafanaDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var grafanaDatabaseTable = GrafanaDatabaseTable.create(connection,
      keyspace);
    grafanaDatabaseTable.createIfNotExists();
    return grafanaDatabaseTable;
  }
}
