package net.taskwolf.core.tutorial.level;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class TutorialLevelRegistry {
  private final List<TutorialLevel> levels = Lists.newArrayList();

  /**
   * Registers a new tutorial level
   * @param level The level to be registered
   */
  public void registerLevel(TutorialLevel level) {
    levels.add(level);
  }

  /**
   * Unregisters a tutorial level
   * @param level The level that will be unregistered
   */
  public void unregisterLevel(TutorialLevel level) {
    levels.remove(level);
  }

  /**
   * Is used to find a tutorial level by index
   * @param index The index of the level
   * @return The tutorial level if it could be found
   */
  public TutorialLevel findLevelByIndex(int index) {
    return levels.get(index);
  }

  /**
   * Is used to find a tutorial level by class
   * @param target The class of the level
   * @return The tutorial level if it could be found
   */
  public TutorialLevel findLevelByClass(Class<? extends TutorialLevel> target) {
    return levels.stream()
      .filter(level -> level.getClass().equals(target))
      .findFirst().get();
  }

  /**
   * Is used to find all levels that are registered
   * @return The list of all tutorial levels
   */
  public List<TutorialLevel> findAll() {
    return List.copyOf(levels);
  }

  /**
   * Is used to find the index of step in all steps of all levels
   * @param target The target step
   * @param exclusions Levels that should not be included in the calculation
   * @return The index of the step
   */
  public int findStepProgress(
    TutorialStep target, Class<? extends TutorialLevel>[] exclusions
  ) {
    var progress = 0;
    for (var level : levels) {
      var isExcluded = Arrays.stream(exclusions)
        .anyMatch(exclusion -> exclusion.equals(level.getClass()));
      if (isExcluded) {
        continue;
      }
       for (var step : level.steps()) {
         progress++;
         if (step.equals(target)) {
           return progress;
         }
       }
    }
    return progress;
  }

  /**
   * Is used to find the number of all steps
   * @param exclusions Levels that should not be included in the calculation
   * @return The number of the steps inside the registered levels
   */
  public int findStepNumber(Class<? extends TutorialLevel>[] exclusions) {
    var number = 0;
    for (var level : levels) {
      var isExcluded = Arrays.stream(exclusions)
        .anyMatch(exclusion -> exclusion.equals(level.getClass()));
      if (isExcluded) {
        continue;
      }
      number += level.steps().size();
    }
    return number;
  }

  /**
   * Is used to find the number of levels
   * @return The number of levels
   */
  public int size() {
    return levels.size();
  }
}
