package net.taskwolf.core.stripe;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;

@RequiredArgsConstructor(staticName = "create")
public final class StripeInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  StripeConfiguration provideStripeConfiguration() throws Exception {
    return StripeConfiguration.createAndLoad();
  }

  @Provides
  @Singleton
  StripeDatabaseTable provideStripeDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var stripeDatabaseTable = StripeDatabaseTable.create(connection,
      keyspace);
    stripeDatabaseTable.createIfNotExists();
    return stripeDatabaseTable;
  }
}
