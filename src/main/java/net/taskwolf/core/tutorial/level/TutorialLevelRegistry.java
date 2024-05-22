package net.taskwolf.core.tutorial.level;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

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
   * Is used to find a tutorial level by id
   * @param index The index of the level
   * @return The tutorial level if it could be found
   */
  public TutorialLevel findLevel(int index) {
    return levels.get(index);
  }

  /**
   * Is used to find all levels that are registered
   * @return The list of all tutorial levels
   */
  public List<TutorialLevel> findAll() {
    return List.copyOf(levels);
  }

  /**
   * Is used to find the number of levels
   * @return The number of levels
   */
  public int size() {
    return levels.size();
  }
}
