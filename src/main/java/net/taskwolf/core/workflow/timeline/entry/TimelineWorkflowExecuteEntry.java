package net.taskwolf.core.workflow.timeline.entry;

public final class TimelineWorkflowExecuteEntry extends TimelineEntry {
  public static TimelineWorkflowExecuteEntry create(long time) {
    return new TimelineWorkflowExecuteEntry(time);
  }

  private TimelineWorkflowExecuteEntry(long time) {
    super(time);
  }

  @Override
  public String title() {
    return "Workflow was executed";
  }

  @Override
  public String description() {
    return "The workflow was triggered and the actions were executed.";
  }

  @Override
  public TimelineEntryLevel level() {
    return TimelineEntryLevel.SUCCESS;
  }
}
