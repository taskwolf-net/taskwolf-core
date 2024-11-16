package com.dulno.core.error;

import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public final class ErrorInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  ErrorDatabaseTable provideErrorDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var errorDatabaseTable = ErrorDatabaseTable.create(connection,
      keyspace);
    errorDatabaseTable.createIfNotExists();
    errorDatabaseTable.createIndexIfNotExists("state");
    return errorDatabaseTable;
  }
}
