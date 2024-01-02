package net.taskwolf.core.workflow.timeline.entry;

import net.taskwolf.core.user.UserDatabaseTable;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class TimelineWorkflowActionRemoveEntry extends TimelineEntry {
  public static CompletableFuture<TimelineEntry> of(
    long time, UserDatabaseTable userDatabaseTable, JSONObject content
  ) {
    return userDatabaseTable.findUserIfExists(UUID.fromString(content.getString("actor")))
      .thenApply(user -> create(time, user.name()));
  }

  public static TimelineWorkflowActionRemoveEntry create(long time, String creator) {
    return new TimelineWorkflowActionRemoveEntry(time, creator);
  }

  private final String actor;

  private TimelineWorkflowActionRemoveEntry(long time, String actor) {
    super(time);
    this.actor = actor;
  }

  @Override
  public String title() {
    return "Workflow action removed";
  }

  @Override
  public String description() {
    return "An action was removed from the workflow by " + actor + ".";
  }

  @Override
  public TimelineEntryLevel level() {
    return TimelineEntryLevel.INFO;
  }
}
