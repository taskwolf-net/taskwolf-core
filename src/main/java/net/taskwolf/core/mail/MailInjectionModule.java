package net.taskwolf.core.mail;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;

@RequiredArgsConstructor(staticName = "create")
public final class MailInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  OutgoingMailDatabaseTable provideOutgoingMailDatabaseTable(
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    return OutgoingMailDatabaseTable.create(databaseConnection, databaseKeyspace);
  }

  @Provides
  @Singleton
  MailTemplate provideMailTemplate() throws Exception {
    return MailTemplate.createAndLoad();
  }
}
