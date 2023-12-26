package net.taskwolf.core.workflow;

import com.google.common.collect.Multimap;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.action.Action;
import net.taskwolf.core.condition.Condition;
import net.taskwolf.core.workflow.timeline.TimelineDatabaseTable;
import org.json.JSONObject;

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
        return;
      }
      var result = actions.get(i).execute(information);
      if (result.isFailure()) {
        postExecutionFailure(result.failureMessage());
        return;
      }
      information.putAll(result.information());
    }
    postExecutionSuccess();
  }

  private void postExecutionSuccess() {
    long currentTime = System.currentTimeMillis();
    workflowExecutionDatabaseTable.addWorkflowExecution(workflowId, currentTime);
    timelineDatabaseTable.generateAvailableEntryId().thenAccept(id ->
      timelineDatabaseTable.insertEntry(id, workflowId, currentTime,
        "timeline-workflow-execute", "{}"));
  }

  private void postExecutionFailure(String failureMessage) {
    long currentTime = System.currentTimeMillis();
    //TODO: Mark workflow as failed in WorkflowDatabaseTable (for dashboard display)
    timelineDatabaseTable.generateAvailableEntryId().thenAccept(id ->
      timelineDatabaseTable.insertEntry(id, workflowId, currentTime,
        "timeline-workflow-failure", new JSONObject(Map.of("message",
          failureMessage)).toString()));
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
