package net.taskwolf.core.trigger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;

@RequiredArgsConstructor(staticName = "create")
public final class TriggerInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  TriggerDatabaseTable provideTriggerDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var triggerDatabaseTable = TriggerDatabaseTable.create(connection, keyspace);
    triggerDatabaseTable.createIfNotExists();
    return triggerDatabaseTable;
  }
}
