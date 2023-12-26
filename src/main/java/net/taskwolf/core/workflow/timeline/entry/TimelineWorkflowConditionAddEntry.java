package net.taskwolf.core.workflow.timeline.entry;

import net.taskwolf.core.user.UserDatabaseTable;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class TimelineWorkflowConditionAddEntry extends TimelineEntry {
  public static CompletableFuture<TimelineEntry> of(
    long time, UserDatabaseTable userDatabaseTable, JSONObject content
  ) {
    return userDatabaseTable.findUser(UUID.fromString(content.getString("actor")))
      .thenApply(user -> create(time, user.name()));
  }

  public static TimelineWorkflowConditionAddEntry create(long time, String creator) {
    return new TimelineWorkflowConditionAddEntry(time, creator);
  }

  private final String actor;

  private TimelineWorkflowConditionAddEntry(long time, String actor) {
    super(time);
    this.actor = actor;
  }

  @Override
  public String title() {
    return "Workflow condition added";
  }

  @Override
  public String description() {
    return "A condition was added to the workflow by " + actor + ".";
  }

  @Override
  public TimelineEntryLevel level() {
    return TimelineEntryLevel.INFO;
  }
}
