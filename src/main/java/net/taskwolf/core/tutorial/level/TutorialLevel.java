package net.taskwolf.core.tutorial.level;

import java.util.List;

public interface TutorialLevel {
  /**
   * The list of page URLs on which the level takes place
   * @return The list of URLs that belongs to the tutorial step
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
