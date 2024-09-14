package com.dulno.core.mail;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;

@RequiredArgsConstructor(staticName = "create")
public final class MailInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  OutgoingMailDatabaseTable provideOutgoingMailDatabaseTable(
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    return OutgoingMailDatabaseTable.create(databaseConnection, databaseKeyspace);
  }
}
