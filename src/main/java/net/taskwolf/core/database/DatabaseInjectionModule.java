package net.taskwolf.core.database;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public final class DatabaseInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  DatabaseConfiguration provideDatabaseConfiguration() throws Exception {
    return DatabaseConfiguration.createAndLoad();
  }

  @Provides
  @Singleton
  DatabaseConnection provideDatabaseConnection(DatabaseConfiguration configuration) {
    var databaseConnection = DatabaseConnection.create(configuration);
    databaseConnection.connect();
    return databaseConnection;
  }

  @Provides
  @Singleton
  DatabaseKeyspace provideDatabaseKeyspace(DatabaseConnection connection) {
    var databaseKeyspace = DatabaseKeyspace.create(connection, "taskwolf",
      "SimpleStrategy", 1);
    databaseKeyspace.createIfNotExists().join();
    databaseKeyspace.use();
    return databaseKeyspace;
  }
}
