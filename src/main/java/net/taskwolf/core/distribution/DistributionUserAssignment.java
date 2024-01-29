package net.taskwolf.core.distribution;

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
public final class DistributionUserAssignment {
  private final Multimap<String, UUID> assignment = HashMultimap.create();

  public void assignUser(String module, UUID user) {
    assignment.put(module, user);
  }

  public void assignUsers(String module, List<UUID> users) {
    assignment.putAll(module, users);
  }

  public void removeUser(String module, UUID user) {
    assignment.remove(module, user);
  }

  public void deleteModule(String module) {
    assignment.removeAll(module);
  }

  public List<UUID> findAssignedUsers(String module) {
    return Lists.newArrayList(assignment.get(module));
  }

  public boolean isAssignedUser(String module, UUID user) {
    return assignment.containsEntry(module, user);
  }

  public List<String> findModulesAssignedTo(UUID user) {
    var modules = Lists.<String>newArrayList();
    for (var module : assignment.keys()) {
      if (assignment.containsEntry(module, user)) {
        modules.add(module);
      }
    }
    return modules;
  }

  public List<String> findAllModules() {
    return Lists.newArrayList(assignment.keys());
  }
}
