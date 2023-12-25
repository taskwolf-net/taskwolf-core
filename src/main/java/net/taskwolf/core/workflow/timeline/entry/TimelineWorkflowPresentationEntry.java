package net.taskwolf.core.workflow.timeline.entry;

import net.taskwolf.core.user.UserDatabaseTable;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class TimelineWorkflowPresentationEntry extends TimelineEntry {
  public static CompletableFuture<TimelineEntry> of(
    long time, UserDatabaseTable userDatabaseTable, JSONObject content
  ) {
    return userDatabaseTable.findUser(UUID.fromString(content.getString("actor")))
      .thenApply(user -> create(time, user.name()));
  }

  public static TimelineWorkflowPresentationEntry create(long time, String creator) {
    return new TimelineWorkflowPresentationEntry(time, creator);
  }

  private final String actor;

  private TimelineWorkflowPresentationEntry(long time, String actor) {
    super(time);
    this.actor = actor;
  }

  @Override
  public String title() {
    return "Workflow presentation changed";
  }

  @Override
  public String description() {
    return actor + " has changed the name or description of the workflow.";
  }
}