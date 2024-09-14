package com.dulno.core.stripe;

import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.stripe.StripeClient;
import lombok.RequiredArgsConstructor;

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
    stripeDatabaseTable.createIndexIfNotExists("target");
    return stripeDatabaseTable;
  }

  @Provides
  @Singleton
  TerminationDatabaseTable provideTerminationDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var terminationDatabaseTable = TerminationDatabaseTable.create(connection,
      keyspace);
    terminationDatabaseTable.createIfNotExists();
    return terminationDatabaseTable;
  }

  @Provides
  @Singleton
  StripeClient provideStripeClient(StripeConfiguration configuration) {
    return new StripeClient(configuration.apiKey());
  }
}
