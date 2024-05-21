package net.taskwolf.core.tutorial.level;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TutorialStep {
  /**
   * Is a sub-unit of the tutorial level. Is used to display individual hints
   * and tasks in a level.
   * @param title The title that is shown when the tutorial level is reached
   *              (It is best to pass the locales key)
   * @param description The title that is shown when the tutorial level is reached
   *                    (It is best to pass the locales key)
   * @param element The html id of the element to be highlighted on the website.
   *                If no element is to be highlighted, an empty string should
   *                be handed over
   * @return The tutorial step
   */
  public static TutorialStep create(String title, String description, String element) {
    return new TutorialStep(title, description, element);
  }

  private final String title;
  private final String description;
  private final String element;
}
