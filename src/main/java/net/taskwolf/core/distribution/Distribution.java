package net.taskwolf.core.distribution;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.client.DistributionClient;
import net.taskwolf.core.distribution.client.DistributionClientRegistry;
import net.taskwolf.core.distribution.client.packet.node.PacketOutgoingDisconnect;
import net.taskwolf.core.distribution.client.packet.node.PacketOutgoingHandshakeRequest;
import net.taskwolf.core.distribution.client.packet.node.PacketOutgoingModuleLoad;
import net.taskwolf.core.distribution.client.packet.node.PacketOutgoingModuleUnload;
import net.taskwolf.core.distribution.client.packet.user.PacketOutgoingUserDelete;
import net.taskwolf.core.distribution.client.packet.user.PacketOutgoingUsersReorganize;
import net.taskwolf.core.distribution.packet.PacketRegistry;
import net.taskwolf.core.distribution.server.DistributionServer;
import net.taskwolf.core.event.EventExecutor;
import net.taskwolf.core.organization.Organization;
import net.taskwolf.core.organization.OrganizationDatabaseTable;
import net.taskwolf.core.user.User;
import net.taskwolf.core.user.UserDatabaseTable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class Distribution {
  private final UserDatabaseTable userDatabaseTable;
  private final OrganizationDatabaseTable organizationDatabaseTable;
  private final DistributionConfiguration configuration;
  private final PacketRegistry packetRegistry;
  private final EventExecutor eventExecutor;
  private final DistributionClientRegistry clientRegistry;
  private final DistributionUserAssignment userAssignment;
  private DistributionServer server;

  public void initialize() {
    server = DistributionServer.create(configuration, packetRegistry,
      eventExecutor, clientRegistry, configuration.self().distributionPort());
    server.openAsync(this::connectToNodes);
  }

  private void connectToNodes() {
    for (var node : configuration.nodes()) {
      var client = DistributionClient.create(configuration, packetRegistry,
        eventExecutor, clientRegistry, node);
      clientRegistry.registerClient(client);
      client.connectAsync(() -> client.sendPacket(new PacketOutgoingHandshakeRequest(
        node.hostname(), node.distributionPort(), node.distributionKey())));
    }
  }

  public void registerModule(String module) {
    server.broadcastPacket(new PacketOutgoingModuleLoad(module));
    reorganizeUsers(module);
  }

  public void addUser(UUID user) {
    for (var module : userAssignment.findAllModules()) {
      userAssignment.assignUser(module, user);
    }
  }

  public void removeUser(UUID user) {
    for (var module : userAssignment.findModulesAssignedTo(user)) {
      userAssignment.removeUser(module, user);
    }
    server.broadcastPacket(new PacketOutgoingUserDelete(user));
  }

  public void unregisterModule(String module) {
    server.broadcastPacket(new PacketOutgoingModuleUnload(module));
    reorganizeUsers(module);
  }

  private void reorganizeUsers(String module) {
    reassignUsers(module).thenAccept(assignment -> assignment.keySet().forEach(
      client -> client.sendPacket(new PacketOutgoingUsersReorganize(module,
        Lists.newArrayList(assignment.get(client))))));
  }

  private CompletableFuture<Multimap<DistributionClient, UUID>> reassignUsers(
    String module
  ) {
    return findAllPossibleUser().thenApply(users -> reassignUsers(module, users));
  }

  private Multimap<DistributionClient, UUID> reassignUsers(
    String module, List<UUID> allUsers
  ) {
    /*var result = HashMultimap.<DistributionClient, UUID>create();
    int size = (int) Math.floor((double) allUsers.size() / moduleNodes.size());
    int currentNode = 0;
    for (var start = 0; start < allUsers.size(); start += size) {
      var end = Math.min(start + size, allUsers.size());
      assignUsersToModule(moduleNodes.get(currentNode), module,
        allPossibleUsers.subList(start, end));
      currentNode++;
    }
    return result;*/
    return null;
  }

  private CompletableFuture<List<UUID>> findAllPossibleUser() {
    var futureResponse = new CompletableFuture<List<UUID>>();
    userDatabaseTable.findAllUsers().thenAccept(users ->
      organizationDatabaseTable.findAllOrganization().thenAccept(organizations ->
        futureResponse.complete(Stream.concat(users.stream().map(User::id),
          organizations.stream().map(Organization::id)).collect(Collectors.toList()))));
    return futureResponse;
  }

  public boolean isAssignedUser(String module, UUID user) {
    return userAssignment.isAssignedUser(module, user);
  }

  public List<UUID> findAssignedUsers(String module) {
    return userAssignment.findAssignedUsers(module);
  }

  public List<String> findConnectedNodes() {
    return clientRegistry.findAllClients().stream().map(DistributionClient::node)
      .map(node -> node.hostname() + " [" + node.type() + "]").toList();
  }

  public void disconnect() {
    server.broadcastPacket(new PacketOutgoingDisconnect());
    server.close();
  }
}
