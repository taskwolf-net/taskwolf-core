package net.taskwolf.core.workflow.timeline.entry;

import net.taskwolf.core.CoreModule;
import net.taskwolf.core.user.User;

public final class TimelineWorkflowExecuteEntry extends TimelineEntry {
  public static TimelineWorkflowExecuteEntry create(long time) {
    return new TimelineWorkflowExecuteEntry(time);
  }

  private TimelineWorkflowExecuteEntry(long time) {
    super(time);
  }

  @Override
  public String title(CoreModule coreModule, User user) {
    return coreModule.translate(user, "workflow.timeline.entry.executed.title");
  }

  @Override
  public String description(CoreModule coreModule, User user) {
    return coreModule.translate(user, "workflow.timeline.entry.executed.description");
  }

  @Override
  public TimelineEntryLevel level() {
    return TimelineEntryLevel.SUCCESS;
  }
}
