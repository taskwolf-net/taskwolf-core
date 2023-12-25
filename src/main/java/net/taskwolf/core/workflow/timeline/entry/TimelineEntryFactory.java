package net.taskwolf.core.workflow.timeline.entry;

import lombok.RequiredArgsConstructor;
import net.taskwolf.core.user.UserDatabaseTable;
import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public final class TimelineEntryFactory {
  private final UserDatabaseTable userDatabaseTable;

  public CompletableFuture<TimelineEntry> create(long time, String type, String content) {
    var json = new JSONObject(content);
    if (type.equals("timeline-workflow-create")) {
      return TimelineWorkflowCreateEntry.of(time, userDatabaseTable, json);
    }
    if (type.equals("timeline-workflow-execute")) {
      return CompletableFuture.completedFuture(TimelineWorkflowExecuteEntry.create(time));
    }
    if (type.equals("timeline-workflow-presentation")) {
      return TimelineWorkflowPresentationEntry.of(time, userDatabaseTable, json);
    }
    if (type.equals("timeline-workflow-action-add")) {
      return TimelineWorkflowActionAddEntry.of(time, userDatabaseTable, json);
    }
    if (type.equals("timeline-workflow-action-remove")) {
      return TimelineWorkflowActionRemoveEntry.of(time, userDatabaseTable, json);
    }
    if (type.equals("timeline-workflow-condition-add")) {
      return TimelineWorkflowConditionAddEntry.of(time, userDatabaseTable, json);
    }
    if (type.equals("timeline-workflow-condition-remove")) {
      return TimelineWorkflowConditionRemoveEntry.of(time, userDatabaseTable, json);
    }
    return null;
  }
}
