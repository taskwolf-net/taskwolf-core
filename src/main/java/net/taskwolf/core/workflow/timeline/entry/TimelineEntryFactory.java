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
    return null;
  }
}
