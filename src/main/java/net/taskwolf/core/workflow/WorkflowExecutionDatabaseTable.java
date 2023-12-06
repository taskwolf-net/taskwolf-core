package net.taskwolf.core.workflow;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class WorkflowExecutionDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "workflow_execution";

  public static WorkflowExecutionDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("workflow", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseListColumn.create("executions", DatabaseDataType.BIGINT));
    return new WorkflowExecutionDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private WorkflowExecutionDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void addWorkflowExecution(UUID workflowId, long execution) {
    exists(DatabaseCell.create(workflowId)).thenAccept(exists ->
      addWorkflowExecution(workflowId, execution, exists));
  }

  private void addWorkflowExecution(UUID workflowId, long execution, boolean exists) {
    if (!exists) {
      addWorkflowExecution(workflowId, execution);
      return;
    }
    selectRow(DatabaseCell.create(workflowId)).thenAccept(row ->
      addWorkflowExecution(workflowId, execution, row));
  }

  private void addWorkflowExecution(UUID workflowId, long execution, DatabaseRow row) {
    var executions = row.findCell(1).<Long>listValue();
    executions.add(execution);
    updateWorkflowExecutions(workflowId, executions);
  }

  private void insertWorkflowExecution(UUID workflowId, long execution) {
    insertWorkflowExecution(workflowId, Lists.newArrayList(execution));
  }

  public void insertWorkflowExecution(UUID workflowId, List<Long> executions) {
    insert(DatabaseRow.of(workflowId, executions));
  }

  public void removeWorkflowExecution(UUID workflowId, long execution) {
    selectRow(DatabaseCell.create(workflowId)).thenAccept(row ->
      removeWorkflowExecution(workflowId, execution, row));
  }

  private void removeWorkflowExecution(UUID workflowId, long execution, DatabaseRow row) {
    var executions = row.findCell(1).<Long>listValue();
    if (executions.size() == 1) {
      deleteWorkflowExecutions(workflowId);
      return;
    }
    executions.remove(execution);
    updateWorkflowExecutions(workflowId, executions);
  }

  private void updateWorkflowExecutions(UUID workflowId, List<Long> executions) {
    update(DatabaseCell.create(workflowId), DatabaseRow.of(workflowId, executions));
  }

  public void deleteWorkflowExecutions(UUID workflowId) {
    delete(DatabaseCell.create(workflowId));
  }

  public CompletableFuture<Boolean> workflowExecutionExists(UUID workflowId) {
    return exists(DatabaseCell.create(workflowId));
  }

  public CompletableFuture<List<Long>> findWorkflowExecutions(UUID workflowId) {
    return selectRow(DatabaseCell.create(workflowId)).thenApply(row ->
      row.findCell(1).listValue());
  }
}