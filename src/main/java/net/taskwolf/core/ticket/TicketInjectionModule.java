package net.taskwolf.core.ticket;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;

@RequiredArgsConstructor(staticName = "create")
public final class TicketInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  TicketDatabaseTable provideTicketDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var ticketDatabaseTable = TicketDatabaseTable.create(connection, keyspace);
    ticketDatabaseTable.createIfNotExists();
    ticketDatabaseTable.createIndexIfNotExists("creator");
    return ticketDatabaseTable;
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
