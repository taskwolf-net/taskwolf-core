package net.taskwolf.core.worker;

import com.datastax.oss.driver.shaded.guava.common.collect.HashMultimap;
import com.datastax.oss.driver.shaded.guava.common.collect.Multimap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class WorkerUserAssignment {
  private final Multimap<String, UUID> assignment = HashMultimap.create();

  /**
   * Assigns new user to certain module
   * @param module The module that the user will be assigned to
   * @param user The new user that will be assigned to module
   */
  public void assignUser(String module, UUID user) {
    assignment.put(module, user);
  }

  /**
   * Assigns multiple users to module
   * @param module The module that the users will be assigned to
   * @param users The users that will be assigned to module
   */
  public void assignUsers(String module, List<UUID> users) {
    assignment.putAll(module, users);
  }

  /**
   * Removes user from module
   * @param module The module from which the user will be removed from
   * @param user The user that will be removed from the module
   */
  public void removeUser(String module, UUID user) {
    assignment.remove(module, user);
  }

  /**
   * Deletes a whole module from the assignments
   * @param module The module that will be deleted
   */
  public void deleteModule(String module) {
    assignment.removeAll(module);
  }

  /**
   * Is used to find the users that are assigned to module
   * @param module The module from which the users will be found
   * @return The list of assigned users
   */
  public List<UUID> findAssignedUsers(String module) {
    return Lists.newArrayList(assignment.get(module));
  }

  /**
   * Checks whether a user is assigned to module
   * @param module The module that will be checked
   * @param user The user that will be checked
   * @return Is true if user is assigned to module, otherwise false
   */
  public boolean isAssignedUser(String module, UUID user) {
    return assignment.containsEntry(module, user);
  }

  /**
   * Is used to find all modules a user is assigned to
   * @param user The user that will be checked
   * @return The list of modules the user is assigned to
   */
  public List<String> findModulesAssignedTo(UUID user) {
    var modules = Lists.<String>newArrayList();
    for (var module : assignment.keys()) {
      if (assignment.containsEntry(module, user)) {
        modules.add(module);
      }
    }
    return modules;
  }

  /**
   * Is used to find all available modules
   * @return The list of modules
   */
  public List<String> findAllModules() {
    return Lists.newArrayList(assignment.keys());
  }
}
