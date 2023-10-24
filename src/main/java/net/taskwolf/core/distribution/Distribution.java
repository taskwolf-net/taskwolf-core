package net.taskwolf.core.distribution;

import ch.qos.logback.classic.LoggerContext;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.iterator.AsyncAllocationIterator;
import net.taskwolf.core.iterator.AsyncIterator;
import net.taskwolf.core.iterator.AsyncListIterator;
import net.taskwolf.core.organization.Organization;
import net.taskwolf.core.organization.OrganizationDatabaseTable;
import net.taskwolf.core.user.User;
import net.taskwolf.core.user.UserDatabaseTable;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor(staticName = "create")
public final class Distribution {
  private final DistributionConfiguration distributionConfiguration;
  private final UserDatabaseTable userDatabaseTable;
  private final OrganizationDatabaseTable organizationDatabaseTable;
  private UUID localIdentifier;
  private RedissonClient redisson;

  public void initialize() {
    disableRedissonLogs();
    //TODO: CHECK IF LOCAL IDENTIFIER IS ALREADY NODE IDENTIFIER
    localIdentifier = UUID.randomUUID();
    var config = new Config();
    var clusterConfig = config.useClusterServers();
    var self = distributionConfiguration.self();
    clusterConfig.addNodeAddress(createNodeAddress(self));
    for (var node : distributionConfiguration.nodes()) {
      clusterConfig.addNodeAddress(createNodeAddress(node));
    }
    redisson = Redisson.create(config);
    redisson.getList("taskwolf-nodes").addAsync(localIdentifier.toString());
    redisson.getBucket("taskwolf-" + localIdentifier.toString() + "-hostname")
      .setAsync(self.hostname() + ":" + self.redisPort());
    registerModule("core");
    Runtime.getRuntime().addShutdownHook(new Thread(this::destroy));
  }

  private void disableRedissonLogs() {
    ((LoggerContext) LoggerFactory.getILoggerFactory())
      .getLogger("org.redisson").setLevel(ch.qos.logback.classic.Level.ERROR);
  }

  private String createNodeAddress(Node node) {
    return "redis://" + node.hostname() + ":" + node.redisPort();
  }

  public void registerModule(String name) {
    redisson.getList("taskwolf-" + localIdentifier + "-modules").addAsync(name)
      .thenAccept(value -> findAllPossibleUser().thenAccept(users ->
        findAllRegisteredModules().thenAccept(modules ->
          completeModuleRegistration(name, users, modules))));
  }

  private void completeModuleRegistration(
    String name, List<UUID> allPossibleUsers, List<String> allRegisteredModules
  ) {
    if (!allRegisteredModules.contains(name)) {
      assignUsersToModule(localIdentifier, name, allPossibleUsers);
      return;
    }
    reorganizeModuleUsers(name, allPossibleUsers);
  }

  public void addNewUser(UUID user) {
    findRegisteredModules(localIdentifier).thenAccept(modules ->
      modules.forEach(module -> redisson.getList("taskwolf-" + localIdentifier
        + "-" + module).addAsync(user.toString())));
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
    redisson.getBucket("taskwolf-" + localIdentifier.toString() + "-hostname")
      .deleteAsync();
    redisson.getList("taskwolf-nodes").removeAsync(localIdentifier.toString())
      .thenAccept(value -> findRegisteredModules(localIdentifier)
        .thenAccept(modules -> modules.forEach(module ->
          unregisterModule(module, allPossibleUsers))));
  }

  private void unregisterModule(String module, List<UUID> allPossibleUsers) {
    redisson.getList("taskwolf-" + localIdentifier + "-" + module).deleteAsync();
    redisson.getList("taskwolf-" + localIdentifier + "-modules").removeAsync(module)
      .thenAccept(value -> reorganizeModuleUsers(module, allPossibleUsers));
  }

  private void reorganizeModuleUsers(String module, List<UUID> allPossibleUsers) {
    findNodesWithModule(module).thenAccept(nodes ->
      reorganizeModuleUsers(module, allPossibleUsers, nodes));
  }

  private void reorganizeModuleUsers(
    String module, List<UUID> allPossibleUsers, List<UUID> moduleNodes
  ) {
    int size = (int) Math.floor((double) allPossibleUsers.size() / moduleNodes.size());
    int currentNode = 0;
    for (var start = 0; start < allPossibleUsers.size(); start += size) {
      var end = Math.min(start + size, allPossibleUsers.size());
      assignUsersToModule(moduleNodes.get(currentNode), module,
        allPossibleUsers.subList(start, end));
      currentNode++;
    }
  }

  private void assignUsersToModule(
    UUID node, String moduleName, List<UUID> users
  ) {
    var list = redisson.getList("taskwolf-" + node.toString() + "-" + moduleName);
    list.deleteAsync().thenAccept(value -> list.addAllAsync(users));
  }

  private CompletableFuture<List<UUID>> findAllPossibleUser() {
    var futureResponse = new CompletableFuture<List<UUID>>();
    userDatabaseTable.findAllUsers().thenAccept(users ->
      organizationDatabaseTable.findAllOrganization().thenAccept(organizations ->
        futureResponse.complete(Stream.concat(users.stream().map(User::id),
          organizations.stream().map(Organization::id)).collect(Collectors.toList()))));
    return futureResponse;
  }

  public CompletableFuture<Boolean> isAssignedUser(String moduleName, UUID userId) {
    var futureResponse = new CompletableFuture<Boolean>();
    findAssignedUsers(moduleName).thenAccept(users ->
      futureResponse.complete(users.stream().anyMatch(assigned ->
        assigned.equals(userId))));
    return futureResponse;
  }

  public CompletableFuture<List<UUID>> findAssignedUsers(String moduleName) {
    return findRedisList("taskwolf-" + localIdentifier + "-" + moduleName)
      .thenApply(nodes -> nodes.stream().map(UUID::fromString).collect(Collectors.toList()));
  }

  private CompletableFuture<List<UUID>> findNodesWithModule(String module) {
    var futureResponse = new CompletableFuture<List<UUID>>();
    findAllNodes().thenAccept(nodes -> AsyncAllocationIterator.execute(nodes,
      this::findRegisteredModules, nodes.size(), nodeModules ->
        futureResponse.complete(filterNodesWithModules(module, nodeModules))));
    return futureResponse;
  }

  private List<UUID> filterNodesWithModules(
    String module, Map<UUID, List<String>> nodeModules
  ) {
    return nodeModules.entrySet().stream()
      .filter(entry -> entry.getValue().contains(module))
      .map(Map.Entry::getKey).collect(Collectors.toList());
  }

  private CompletableFuture<List<String>> findAllRegisteredModules() {
    var futureResponse = new CompletableFuture<List<String>>();
    findOtherNodes().thenAccept(nodes -> AsyncListIterator.execute(nodes,
      this::findRegisteredModules, nodes.size(), futureResponse::complete));
    return futureResponse;
  }

  private CompletableFuture<List<String>> findRegisteredModules(UUID node) {
    return findRedisList("taskwolf-" + node.toString() + "-modules");
  }

  public CompletableFuture<List<String>> findConnectedNodes() {
    var futureResponse = new CompletableFuture<List<String>>();
    findRedisList("taskwolf-nodes").thenAccept(nodes -> AsyncIterator.execute(nodes,
      node -> redisson.<String>getBucket("taskwolf-" + node + "-hostname")
        .getAsync().toCompletableFuture(), nodes.size(), futureResponse::complete));
    return futureResponse;
  }

  private CompletableFuture<List<UUID>> findOtherNodes() {
    var futureResponse = new CompletableFuture<List<UUID>>();
    findAllNodes().thenAccept(nodes -> futureResponse.complete(nodes.stream()
      .filter(node -> !node.equals(localIdentifier)).collect(Collectors.toList())));
    return futureResponse;
  }

  private CompletableFuture<List<UUID>> findAllNodes() {
    return findRedisList("taskwolf-nodes").thenApply(nodes ->
      nodes.stream().map(UUID::fromString).collect(Collectors.toList()));
  }

  private CompletableFuture<List<String>> findRedisList(String key) {
    var futureResponse = new CompletableFuture<List<String>>();
    var list = redisson.<String>getList(key);
    list.sizeAsync().thenAccept(size -> list.rangeAsync(0, size)
      .thenAccept(futureResponse::complete));
    return futureResponse;
  }
}
