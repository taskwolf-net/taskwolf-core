package com.dulno.core.template;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;

@RequiredArgsConstructor(staticName = "create")
public final class TemplateInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  TemplateDatabaseTable provideTemplateDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    return TemplateDatabaseTable.create(connection, keyspace);
  }
}
