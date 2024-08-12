package net.taskwolf.core.condition;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;

@RequiredArgsConstructor(staticName = "create")
public final class ConditionInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  ConditionDatabaseTable provideConditionDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var conditionDatabaseTable = ConditionDatabaseTable.create(connection,
      keyspace);
    conditionDatabaseTable.createIfNotExists();
    conditionDatabaseTable.createIndexIfNotExists("workflow");
    return conditionDatabaseTable;
  }

  @Provides
  @Singleton
  ConditionInformationRepository provideConditionInformationRepository() {
    return ConditionInformationRepository.create();
  }
}
