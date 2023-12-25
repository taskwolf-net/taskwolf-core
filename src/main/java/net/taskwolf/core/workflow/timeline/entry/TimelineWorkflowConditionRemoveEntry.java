package net.taskwolf.core.workflow.timeline.entry;

import net.taskwolf.core.user.UserDatabaseTable;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class TimelineWorkflowConditionRemoveEntry extends TimelineEntry {
  public static CompletableFuture<TimelineEntry> of(
    long time, UserDatabaseTable userDatabaseTable, JSONObject content
  ) {
    return userDatabaseTable.findUser(UUID.fromString(content.getString("actor")))
      .thenApply(user -> create(time, user.name()));
  }

  public static TimelineWorkflowConditionRemoveEntry create(long time, String creator) {
    return new TimelineWorkflowConditionRemoveEntry(time, creator);
  }

  private final String actor;

  private TimelineWorkflowConditionRemoveEntry(long time, String actor) {
    super(time);
    this.actor = actor;
  }

  @Override
  public String title() {
    return "Workflow condition removed";
  }

  @Override
  public String description() {
    return "A condition was removed from the workflow by " + actor + ".";
  }
}
