package net.taskwolf.core.workflow;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.workflow.timeline.TimelineDatabaseTable;

@RequiredArgsConstructor(staticName = "create")
public final class WorkflowInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  WorkflowDatabaseTable provideWorkflowDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var workflowDatabaseTable = WorkflowDatabaseTable.create(connection,
      keyspace);
    workflowDatabaseTable.createIfNotExists();
    return workflowDatabaseTable;
  }

  @Provides
  @Singleton
  WorkflowExecutionDatabaseTable provideWorkflowExecutionDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var workflowExecutionDatabaseTable = WorkflowExecutionDatabaseTable.create(
      connection, keyspace);
    workflowExecutionDatabaseTable.createIfNotExists();
    return workflowExecutionDatabaseTable;
  }

  @Provides
  @Singleton
  TimelineDatabaseTable provideTimelineDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var timelineDatabaseTable = TimelineDatabaseTable.create(connection,
      keyspace);
    timelineDatabaseTable.createIfNotExists();
    return timelineDatabaseTable;
  }
}
