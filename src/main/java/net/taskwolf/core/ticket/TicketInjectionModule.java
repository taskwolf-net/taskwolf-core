package net.taskwolf.core.ticket;

import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public final class TicketInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  TicketDatabaseTable provideTicketDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    return TicketDatabaseTable.create(connection, keyspace);
  }

  @Provides
  @Singleton
  TicketMessageDatabaseTable provideTicketMessageDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var ticketMessageDatabaseTable = TicketMessageDatabaseTable.create(
      connection, keyspace);
    ticketMessageDatabaseTable.createIfNotExists();
    return ticketMessageDatabaseTable;
  }
}
