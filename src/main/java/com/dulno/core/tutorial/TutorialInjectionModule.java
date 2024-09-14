package com.dulno.core.tutorial;

import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public final class TutorialInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  TutorialDatabaseTable provideTutorialDatabaseTable(
          DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var tutorialDatabaseTable = TutorialDatabaseTable.create(connection, keyspace);
    tutorialDatabaseTable.createIfNotExists();
    return tutorialDatabaseTable;
  }
}
