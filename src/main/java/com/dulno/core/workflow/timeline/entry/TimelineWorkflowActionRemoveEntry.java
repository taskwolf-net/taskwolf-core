package com.dulno.core.workflow.timeline.entry;

import com.dulno.core.CoreModule;
import com.dulno.core.user.User;
import com.dulno.core.user.UserDatabaseTable;
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
  public String title(CoreModule coreModule, User user) {
    return coreModule.translate(user, "workflow.timeline.entry.action.remove.title");
  }

  @Override
  public String description(CoreModule coreModule, User user) {
    return coreModule.translate(user, "workflow.timeline.entry.action.remove.description")
      .replace("%ACTOR%", actor);
  }

  @Override
  public TimelineEntryLevel level() {
    return TimelineEntryLevel.INFO;
  }
}
