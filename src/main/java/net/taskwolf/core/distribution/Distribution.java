package net.taskwolf.core.distribution;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.organization.Organization;
import net.taskwolf.core.organization.OrganizationDatabaseTable;
import net.taskwolf.core.user.User;
import net.taskwolf.core.user.UserDatabaseTable;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor(staticName = "create")
public final class Distribution {
  private final UserDatabaseTable userDatabaseTable;
  private final OrganizationDatabaseTable organizationDatabaseTable;
  private UUID localIdentifier;
  private Jedis jedis;

  public void initialize() {
    //TODO: CHECK IF LOCAL IDENTIFIER IS ALREADY NODE IDENTIFIER
    localIdentifier = UUID.randomUUID();
    jedis = new Jedis("127.0.0.1", 7000);
    jedis.connect();
    jedis.lpush("taskwolf-nodes", localIdentifier.toString());
  }

  public void registerModule(String name) {
    findAllPossibleUser().thenAccept(users -> registerModule(name, users));
  }

  private void registerModule(String name, List<UUID> allPossibleUsers) {
    jedis.lpush("taskwolf-" + localIdentifier + "-modules", name);
    if (!findAllRegisteredModules().contains(name)) {
      assignUsersToModule(name, allPossibleUsers);
      return;
    }
    reorganizeModuleUsers(name, allPossibleUsers);
  }

  public void addNewUser(UUID user) {
    for (var module : findRegisteredModules(localIdentifier)) {
      jedis.lpush("taskwolf-" + localIdentifier + "-" + module, user.toString());
    }
  }

  public void removeUser(UUID user) {
    //TODO: TO BE IMPLEMENTED
  }

  public void unregisterModule(String module) {
    findAllPossibleUser().thenAccept(users -> unregisterModule(module, users));
  }

  public void destroy() {
    findAllPossibleUser().thenAccept(this::destroy);
  }

  private void destroy(List<UUID> allPossibleUsers) {
    jedis.lrem("taskwolf-nodes", 1, localIdentifier.toString());
    for (var module : findRegisteredModules(localIdentifier)) {
      unregisterModule(module, allPossibleUsers);
    }
  }

  private void unregisterModule(String module, List<UUID> allPossibleUsers) {
    jedis.lrem("taskwolf-" + localIdentifier + "-modules", 1, module);
    jedis.del("taskwolf-" + localIdentifier + "-" + module);
    reorganizeModuleUsers(module, allPossibleUsers);
  }

  private void reorganizeModuleUsers(String module, List<UUID> allPossibleUsers) {
    var moduleNodes = findAllNodes().stream().filter(node ->
      findRegisteredModules(node).contains(module)).toList();
    int size = (int) Math.floor((double) allPossibleUsers.size() / moduleNodes.size());
    int currentNode = 0;
    for (var start = 0; start < allPossibleUsers.size(); start += size) {
      var end = Math.min(start + size, allPossibleUsers.size());
      assignUsersToModule(moduleNodes.get(currentNode), module,
        allPossibleUsers.subList(start, end));
      currentNode++;
    }
  }

  private void assignUsersToModule(String moduleName, List<UUID> users) {
    assignUsersToModule(localIdentifier, moduleName, users);
  }

  private void assignUsersToModule(
    UUID node, String moduleName, List<UUID> users
  ) {
    jedis.lpush("taskwolf-" + node.toString() + "-" + moduleName,
      users.stream().map(UUID::toString).toArray(String[]::new));
  }

  private CompletableFuture<List<UUID>> findAllPossibleUser() {
    var futureResponse = new CompletableFuture<List<UUID>>();
    userDatabaseTable.findAllUsers().thenAccept(users ->
      organizationDatabaseTable.findAllOrganization().thenAccept(organizations ->
        Stream.concat(users.stream().map(User::id),
          organizations.stream().map(Organization::id)).collect(Collectors.toList())));
    return futureResponse;
  }

  public boolean isAssignedUser(String moduleName, UUID userId) {
    return findAssignedUsers(moduleName).stream()
      .anyMatch(assigned -> assigned.equals(userId));
  }

  public List<UUID> findAssignedUsers(String moduleName) {
    return findRedisList("taskwolf-" + localIdentifier + "-" + moduleName)
      .stream().map(UUID::fromString).collect(Collectors.toList());
  }

  private List<String> findAllRegisteredModules() {
    var modules = Lists.<String>newArrayList();
    for (var node : findOtherNodes()) {
      modules.addAll(findRegisteredModules(node));
    }
    return modules;
  }

  private List<String> findRegisteredModules(UUID node) {
    return findRedisList("taskwolf-" + node.toString() + "-modules");
  }

  private List<UUID> findOtherNodes() {
    return findAllNodes().stream().filter(node -> !node.equals(localIdentifier))
      .collect(Collectors.toList());
  }

  private List<UUID> findAllNodes() {
    return findRedisList("taskwolf-nodes").stream().map(UUID::fromString)
      .collect(Collectors.toList());
  }

  private List<String> findRedisList(String key) {
    return jedis.lrange(key, 0, jedis.llen(key));
  }
}
