package com.dulno.core.trial;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;

@RequiredArgsConstructor(staticName = "create")
public final class TrialInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  TrialDatabaseTable provideTrialDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var trialDatabaseTable = TrialDatabaseTable.create(connection, keyspace);
    trialDatabaseTable.createIfNotExists();
    return trialDatabaseTable;
  }
}
