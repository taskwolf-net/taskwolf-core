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
   * @param id The id of the level
   * @return The tutorial level if it could be found
   */
  public Optional<TutorialLevel> findLevel(int id) {
    return levels.stream().filter(level -> level.id() == id).findFirst();
  }

  /**
   * Is used to find the highest / last id of a level
   * @return The highest level id
   */
  public Integer findHighestLevelId() {
    return levels.stream().map(TutorialLevel::id).sorted().toList().getLast();
  }

  /**
   * Is used to find all levels that are registered
   * @return The list of all tutorial levels
   */
  public List<TutorialLevel> findAll() {
    return List.copyOf(levels);
  }
}
