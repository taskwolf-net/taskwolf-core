package net.taskwolf.core.tutorial.level;

import java.util.List;

public interface TutorialLevel {
  /**
   * The id for uniquely identifying the level
   * Important: The ID of the level also determines the order in which the
   * levels are played one after the other
   * @return The id of the level
   */
  int id();

  /**
   * The URL of the page on which the level takes place
   * @return The URL that belongs to the tutorial step
   */
  String page();

  /**
   * The individual steps of the level.
   * The order in which the elements can be found in the list corresponds
   * to the order in which the steps are played one after the other.
   * @return The steps of the tutorial
   */
  List<TutorialStep> steps();
}
