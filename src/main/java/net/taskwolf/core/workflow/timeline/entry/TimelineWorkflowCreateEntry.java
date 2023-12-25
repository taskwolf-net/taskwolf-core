package net.taskwolf.core.workflow.timeline.entry;

import net.taskwolf.core.user.UserDatabaseTable;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class TimelineWorkflowCreateEntry extends TimelineEntry {
  public static CompletableFuture<TimelineEntry> of(
    long time, UserDatabaseTable userDatabaseTable, JSONObject content
  ) {
    return userDatabaseTable.findUser(UUID.fromString(content.getString("creator")))
      .thenApply(user -> create(time, user.name()));
  }

  public static TimelineWorkflowCreateEntry create(long time, String creator) {
    return new TimelineWorkflowCreateEntry(time, creator);
  }

  private final String creator;

  private TimelineWorkflowCreateEntry(long time, String creator) {
    super(time);
    this.creator = creator;
  }

  @Override
  public String title() {
    return "Workflow was created";
  }

  @Override
  public String description() {
    return "The workflow was published by " + creator + ".";
  }
}
