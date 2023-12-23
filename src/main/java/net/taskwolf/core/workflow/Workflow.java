package net.taskwolf.core.workflow;

import com.google.common.collect.Multimap;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.action.Action;
import net.taskwolf.core.condition.Condition;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor(staticName = "create")
public final class Workflow {
  private final WorkflowExecutionDatabaseTable workflowExecutionDatabaseTable;
  private final UUID workflowId;
  private final Map<Integer, Action> actions;
  private final Multimap<Integer, Condition> conditions;

  public void trigger(Map<String, Object> information) {
    for (var i = 0; i < actions.size(); i++) {
      if (!checkConditions(i, information)) {
        break;
      }
      information.putAll(actions.get(i).execute(information));
    }
    workflowExecutionDatabaseTable.addWorkflowExecution(workflowId,
      System.currentTimeMillis());
  }

  private boolean checkConditions(int index, Map<String, Object> information) {
    if (!conditions.containsKey(index)) {
      return true;
    }
    var allFulfilled = true;
    for (var condition : conditions.get(index)) {
      if (!condition.compare(information)) {
        allFulfilled = false;
        break;
      }
    }
    return allFulfilled;
  }
}
