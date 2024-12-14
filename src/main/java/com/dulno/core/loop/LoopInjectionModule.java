package com.dulno.core.loop;

import com.dulno.core.condition.ConditionDatabaseTable;
import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public final class LoopInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  ConditionDatabaseTable provideConditionDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    return ConditionDatabaseTable.create(connection, keyspace);
  }

  @Provides
  @Singleton
  LoopInformationRepository provideLoopInformationRepository() {
    return LoopInformationRepository.create();
  }
}
