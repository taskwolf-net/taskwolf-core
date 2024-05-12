package net.taskwolf.core.workflow.timeline.entry;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.CoreModule;
import net.taskwolf.core.user.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class TimelineEntry {
  private final long time;

  /**
   * Creates the title for the timeline entry
   * (must already be translated for the user)
   * @param coreModule Used for translation
   * @param user The user for whom the title is to be translated
   * @return The title of the timeline entry
   */
  public abstract String title(CoreModule coreModule, User user);

  /**
   * Creates the description for the timeline entry
   * (must already be translated for the user)
   * @param coreModule Used for translation
   * @param user The user for whom the description is to be translated
   * @return The description of the timeline entry
   */
  public abstract String description(CoreModule coreModule, User user);

  /**
   * Enables a different representation of different timeline entries
   * (the level primarily determines the color of the entry)
   * @return The level of the timeline entry
   */
  public abstract TimelineEntryLevel level();

  public long rawTime() {
    return time;
  }

  public String formattedTime() {
    var calendar = Calendar.getInstance();
    calendar.setTimeInMillis(time);
    return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(calendar.getTime());
  }
}
