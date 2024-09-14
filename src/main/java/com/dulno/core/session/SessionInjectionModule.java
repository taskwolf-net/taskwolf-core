package com.dulno.core.session;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.maxmind.geoip2.DatabaseReader;
import lombok.RequiredArgsConstructor;
import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;

import java.io.File;

@RequiredArgsConstructor(staticName = "create")
public final class SessionInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  SessionDatabaseTable provideSessionDatabaseTable(
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    return SessionDatabaseTable.create(databaseConnection, databaseKeyspace);
  }

  @Provides
  @Singleton
  DatabaseReader provideDatabaseReader() throws Exception {
    var database = new File(System.getProperty("user.dir") +
      "/geo/GeoLite2-City.mmdb");
    return new DatabaseReader.Builder(database).build();
  }
}
