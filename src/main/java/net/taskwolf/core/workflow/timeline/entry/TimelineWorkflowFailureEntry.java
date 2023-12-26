package net.taskwolf.core.workflow.timeline.entry;

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
  public String title() {
    return "Workflow has failed";
  }

  @Override
  public String description() {
    return message;
  }

  @Override
  public TimelineEntryLevel level() {
    return TimelineEntryLevel.FAILURE;
  }
}