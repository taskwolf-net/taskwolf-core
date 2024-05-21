package net.taskwolf.core.tutorial;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;

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
