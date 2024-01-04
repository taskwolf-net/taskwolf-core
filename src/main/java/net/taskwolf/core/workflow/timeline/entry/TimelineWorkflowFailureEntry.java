package net.taskwolf.core.workflow.timeline.entry;

import net.taskwolf.core.CoreModule;
import net.taskwolf.core.user.User;
import org.json.JSONObject;

public final class TimelineWorkflowFailureEntry extends TimelineEntry {
  public static TimelineWorkflowFailureEntry of(long time, JSONObject content) {
    return create(time, content.getString("message"));
  }

  public static TimelineWorkflowFailureEntry create(long time, String message) {
    return new TimelineWorkflowFailureEntry(time, message);
  }

  private final String message;

  private TimelineWorkflowFailureEntry(long time, String message) {
    super(time);
    this.message = message;
  }

  @Override
  public String title(CoreModule coreModule, User user) {
    return coreModule.translate(user, "workflow.timeline.entry.failed.title");
  }

  @Override
  public String description(CoreModule coreModule, User user) {
    return message;
  }

  @Override
  public TimelineEntryLevel level() {
    return TimelineEntryLevel.FAILURE;
  }
}