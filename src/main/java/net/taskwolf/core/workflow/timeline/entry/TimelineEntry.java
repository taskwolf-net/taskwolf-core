package net.taskwolf.core.workflow.timeline.entry;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class TimelineEntry {
  private final long time;

  public abstract String title();

  public abstract String description();

  public String time() {
    var calendar = Calendar.getInstance();
    calendar.setTimeInMillis(time);
    return new SimpleDateFormat("dd.MM.yyyy").format(calendar.getTime());
  }
}
