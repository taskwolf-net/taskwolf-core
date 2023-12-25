package net.taskwolf.core.workflow;

import com.google.common.collect.Multimap;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.action.Action;
import net.taskwolf.core.condition.Condition;
import net.taskwolf.core.workflow.timeline.TimelineDatabaseTable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor(staticName = "create")
public final class Workflow {
  private final WorkflowExecutionDatabaseTable workflowExecutionDatabaseTable;
  private final TimelineDatabaseTable timelineDatabaseTable;
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
    long currentTime = System.currentTimeMillis();
    workflowExecutionDatabaseTable.addWorkflowExecution(workflowId, currentTime);
    timelineDatabaseTable.generateAvailableEntryId().thenAccept(id ->
      timelineDatabaseTable.insertEntry(id, workflowId, currentTime,
        "timeline-workflow-execute", "{}"));
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
