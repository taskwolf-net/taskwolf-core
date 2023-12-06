package net.taskwolf.core.workflow;

import lombok.RequiredArgsConstructor;
import net.taskwolf.core.action.Action;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor(staticName = "create")
public final class Workflow {
  private final WorkflowExecutionDatabaseTable workflowExecutionDatabaseTable;
  private final UUID workflowId;
  private final List<Action> actions;

  public void trigger(Map<String, Object> information) {
    for (var action : actions) {
      information.putAll(action.execute(information));
    }
    workflowExecutionDatabaseTable.addWorkflowExecution(workflowId,
      System.currentTimeMillis());
  }
}
