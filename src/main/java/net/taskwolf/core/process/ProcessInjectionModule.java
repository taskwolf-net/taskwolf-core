package net.taskwolf.core.process;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.process.connection.ProcessConnectionDatabaseTable;
import net.taskwolf.core.process.step.ProcessStepDatabaseTable;

@RequiredArgsConstructor(staticName = "create")
public final class ProcessInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  ProcessDatabaseTable provideProcessDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var processDatabaseTable = ProcessDatabaseTable.create(connection, keyspace);
    processDatabaseTable.createIfNotExists();
    return processDatabaseTable;
  }

  @Provides
  @Singleton
  ProcessStepDatabaseTable provideProcessStepDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var processStepDatabaseTable = ProcessStepDatabaseTable.create(connection, keyspace);
    processStepDatabaseTable.createIfNotExists();
    return processStepDatabaseTable;
  }

  @Provides
  @Singleton
  ProcessConnectionDatabaseTable provideProcessConnectionDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var processConnectionDatabaseTable = ProcessConnectionDatabaseTable.create(
      connection, keyspace);
    processConnectionDatabaseTable.createIfNotExists();
    return processConnectionDatabaseTable;
  }
}
