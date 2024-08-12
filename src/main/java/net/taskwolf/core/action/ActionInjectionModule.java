package net.taskwolf.core.action;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;

@RequiredArgsConstructor(staticName = "create")
public final class ActionInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  ActionDatabaseTable provideActionDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var actionDatabaseTable = ActionDatabaseTable.create(connection, keyspace);
    actionDatabaseTable.createIfNotExists();
    actionDatabaseTable.createIndexIfNotExists("workflow");
    return actionDatabaseTable;
  }
}