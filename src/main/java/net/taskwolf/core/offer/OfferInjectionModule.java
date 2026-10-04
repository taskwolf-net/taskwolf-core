package net.taskwolf.core.offer;

import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public final class OfferInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  OfferDatabaseTable provideOfferDatabaseTable(
          DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var offerDatabaseTable = OfferDatabaseTable.create(connection, keyspace);
    offerDatabaseTable.createIfNotExists();
    offerDatabaseTable.createIndexIfNotExists("priceId");
    offerDatabaseTable.createIndexIfNotExists("target");
    return offerDatabaseTable;
  }
}
