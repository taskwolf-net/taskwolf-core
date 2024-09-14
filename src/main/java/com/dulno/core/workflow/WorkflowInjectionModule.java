package com.dulno.core.workflow;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;
import com.dulno.core.workflow.operation.OperationDatabaseTable;
import com.dulno.core.workflow.throttle.WorkflowThrottleDatabaseTable;
import com.dulno.core.workflow.timeline.TimelineDatabaseTable;

@RequiredArgsConstructor(staticName = "create")
public final class WorkflowInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  WorkflowDatabaseTable provideWorkflowDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    return WorkflowDatabaseTable.create(connection, keyspace);
  }

  @Provides
  @Singleton
  TimelineDatabaseTable provideTimelineDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var timelineDatabaseTable = TimelineDatabaseTable.create(connection,
      keyspace);
    timelineDatabaseTable.createIfNotExists();
    timelineDatabaseTable.createIndexIfNotExists("workflow");
    return timelineDatabaseTable;
  }

  @Provides
  @Singleton
  OperationDatabaseTable provideOperationDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var operationDatabaseTable = OperationDatabaseTable.create(connection,
      keyspace);
    operationDatabaseTable.createIfNotExists();
    return operationDatabaseTable;
  }

  @Provides
  @Singleton
  WorkflowThrottleDatabaseTable provideWorkflowThrottleDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var workflowThrottleDatabaseTable = WorkflowThrottleDatabaseTable.create(
      connection, keyspace);
    workflowThrottleDatabaseTable.createIfNotExists();
    return workflowThrottleDatabaseTable;
  }
}
