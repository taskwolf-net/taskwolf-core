package net.taskwolf.core.distribution;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Injector;
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
import net.taskwolf.core.distribution.packet.PacketEventRepository;
import net.taskwolf.core.distribution.packet.PacketRegistry;
import net.taskwolf.core.distribution.server.DistributionServer;
import net.taskwolf.core.distribution.server.node.*;
import net.taskwolf.core.distribution.server.packet.node.*;
import net.taskwolf.core.distribution.server.packet.user.PacketIncomingUserDelete;
import net.taskwolf.core.distribution.server.packet.user.PacketIncomingUsersReorganize;
import net.taskwolf.core.distribution.server.user.UserDeleteHook;
import net.taskwolf.core.distribution.server.user.UsersReorganizeHook;
import net.taskwolf.core.event.EventExecutor;
import net.taskwolf.core.event.HookRegistry;
import net.taskwolf.core.event.node.*;
import net.taskwolf.core.event.user.UserDeleteEvent;
import net.taskwolf.core.event.user.UsersReorganizeEvent;
import net.taskwolf.core.log.Log;
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
  private final Injector injector;
  private final UserDatabaseTable userDatabaseTable;
  private final OrganizationDatabaseTable organizationDatabaseTable;
  private final DistributionConfiguration configuration;
  private final PacketRegistry packetRegistry;
  private final EventExecutor eventExecutor;
  private final HookRegistry hookRegistry;
  private final DistributionClientRegistry clientRegistry;
  private final DistributionUserAssignment userAssignment;
  private final PacketEventRepository packetEventRepository;
  private final Log log;
  private DistributionServer server;

  /**
   * Initializes node distribution (registers packets, hooks, events and
   * opens server)
   * @throws Exception
   */
  public void initialize() throws Exception {
    server = DistributionServer.create(configuration, packetRegistry,
      eventExecutor, clientRegistry, packetEventRepository, UUID.randomUUID(),
      configuration.self().distributionPort());
    registerPackets();
    registerHooks();
    registerEvents();
    server.openAsync(this::connectToNodes);
  }

  private void connectToNodes() {
    var hostname = configuration.self().hostname();
    var port = configuration.self().distributionPort();
    for (var node : configuration.nodes()) {
      var client = DistributionClient.create(configuration, packetRegistry,
        eventExecutor, clientRegistry, packetEventRepository, node);
      clientRegistry.registerClient(client);
      client.connectAsync(() -> client.sendPacket(new PacketOutgoingHandshakeRequest(
        hostname, port, node.distributionKey(), server.nodeId())));
    }
  }

  private void registerPackets() throws Exception {
    packetRegistry.registerPacket(PacketIncomingHandshakeRequest.class);
    packetRegistry.registerPacket(PacketIncomingHandshakeResponse.class);
    packetRegistry.registerPacket(PacketIncomingPing.class);
    packetRegistry.registerPacket(PacketIncomingPong.class);
    packetRegistry.registerPacket(PacketIncomingDisconnect.class);
    packetRegistry.registerPacket(PacketIncomingModuleLoad.class);
    packetRegistry.registerPacket(PacketIncomingModuleUnload.class);
    packetRegistry.registerPacket(PacketIncomingUsersReorganize.class);
    packetRegistry.registerPacket(PacketIncomingUserDelete.class);
  }

  private void registerHooks() {
    hookRegistry.register(NodeHandshakeRequestHook.create(configuration,
      packetRegistry, eventExecutor, clientRegistry, packetEventRepository,
      log, server));
    hookRegistry.register(injector.getInstance(NodeHandshakeResponseHook.class));
    hookRegistry.register(injector.getInstance(NodePingHook.class));
    hookRegistry.register(injector.getInstance(NodePongHook.class));
    hookRegistry.register(injector.getInstance(NodeModuleLoadHook.class));
    hookRegistry.register(injector.getInstance(NodeModuleUnloadHook.class));
    hookRegistry.register(NodeDisconnectHook.create(this, clientRegistry,
      server, log));
    hookRegistry.register(injector.getInstance(UsersReorganizeHook.class));
    hookRegistry.register(injector.getInstance(UserDeleteHook.class));
  }

  private void registerEvents() {
    packetEventRepository.registerEvent(PacketIncomingPing.class,
      (client, packet) -> NodePingEvent.create(client, packet.value()));
    packetEventRepository.registerEvent(PacketIncomingPong.class,
      (client, packet) -> NodePongEvent.create(client, packet.value()));
    packetEventRepository.registerEvent(PacketIncomingDisconnect.class,
      (client, packet) -> NodeDisconnectEvent.create(client,
        NodeDisconnectEvent.DisconnectReason.SHUTDOWN));
    packetEventRepository.registerEvent(PacketIncomingModuleLoad.class,
      (client, packet) -> NodeModuleLoadEvent.create(client, packet.module()));
    packetEventRepository.registerEvent(PacketIncomingModuleUnload.class,
      (client, packet) -> NodeModuleUnloadEvent.create(client, packet.module()));
    packetEventRepository.registerEvent(PacketIncomingUsersReorganize.class,
      (client, packet) -> UsersReorganizeEvent.create(packet.module(), packet.users()));
    packetEventRepository.registerEvent(PacketIncomingUserDelete.class,
      (client, packet) -> UserDeleteEvent.create(packet.user()));
  }

  /**
   * Registers a new module
   * @param module The module name
   * @param users The list of users
   */
  public void registerModule(String module, List<UUID> users) {
    server.condition().addModule(module);
    server.broadcastPacket(new PacketOutgoingModuleLoad(module));
    reorganizeUsers(module, users);
  }

  /**
   * Assigns a new user to all modules
   * @param user The new user that will be assigned
   */
  public void addUser(UUID user) {
    for (var module : userAssignment.findAllModules()) {
      userAssignment.assignUser(module, user);
    }
  }

  /**
   * Removes a user from all modules
   * @param user The user that will be removed
   */
  public void removeUser(UUID user) {
    for (var module : userAssignment.findModulesAssignedTo(user)) {
      userAssignment.removeUser(module, user);
    }
    server.broadcastPacket(new PacketOutgoingUserDelete(user));
  }

  /**
   * Unregisters a module
   * @param module The name of the module
   */
  public void unregisterModule(String module) {
    server.condition().removeModule(module);
    server.broadcastPacket(new PacketOutgoingModuleUnload(module));
    reorganizeUsers(module);
  }

  /**
   * Reorganizes users that are assigned to a module in the whole distribution
   * network (on all nodes) (enables constant and equal user distribution)
   * @param module The module that will be reorganized
   */
  public void reorganizeUsers(String module) {
    findAllPossibleUser().thenAccept(users -> reorganizeUsers(module, users));
  }

  private void reorganizeUsers(String module, List<UUID> allUsers) {
    var nodes = clientRegistry.findAllClients().stream().filter(client ->
      client.condition().isModuleLoaded(module)).toList();
    var serverLoadedModule = server.condition().isModuleLoaded(module);
    var nodeCount = nodes.size() + (serverLoadedModule ? 1 : 0);
    var dividedUsers = divideUsers(allUsers, nodeCount);
    if (serverLoadedModule) {
      userAssignment.deleteModule(module);
      userAssignment.assignUsers(module, dividedUsers.get(dividedUsers.size() - 1));
    }
    for (int i = 0; i < nodes.size(); i++) {
      nodes.get(i).sendPacket(new PacketOutgoingUsersReorganize(module,
        dividedUsers.get(i)));
    }
  }

  private List<List<UUID>> divideUsers(List<UUID> allUsers, long nodes) {
    var result = Lists.<List<UUID>>newArrayList();
    int size = (int) Math.floor((double) allUsers.size() / nodes);
    for (var start = 0; start < allUsers.size(); start += size) {
      var end = Math.min(start + size, allUsers.size());
      result.add(allUsers.subList(start, end));
    }
    return result;
  }

  /**
   * Is used to find all user and organization ids
   * @return The list of ids
   */
  public CompletableFuture<List<UUID>> findAllPossibleUser() {
    var futureResponse = new CompletableFuture<List<UUID>>();
    userDatabaseTable.findAllUsers().thenAccept(users ->
      organizationDatabaseTable.findAllOrganization().thenAccept(organizations ->
        futureResponse.complete(Stream.concat(users.stream().map(User::id),
          organizations.stream().map(Organization::id)).collect(Collectors.toList()))));
    return futureResponse;
  }

  /**
   * Checks whether a user is assigned to a module
   * @param module The name of the module
   * @param user The user that will be checked
   * @return Is true, if user is assigned to module, otherwise false
   */
  public boolean isAssignedUser(String module, UUID user) {
    return userAssignment.isAssignedUser(module, user);
  }

  /**
   * Is used to find all assigned users of a module
   * @param module The name of the module
   * @return The list of all assigned users
   */
  public List<UUID> findAssignedUsers(String module) {
    return userAssignment.findAssignedUsers(module);
  }

  /**
   * Is used to find all connected nodes
   * @return The list of node ids
   */
  public List<String> findConnectedNodes() {
    return clientRegistry.findAllClients().stream()
      .map(DistributionClient::node).map(Node::information).toList();
  }

  /**
   * Is used to find all loaded modules
   * @return The list of loaded modules
   */
  public List<String> findLoadedModules() {
    var loadedModule = Lists.<String>newArrayList();
    for (var client : clientRegistry.findAllClients()) {
      loadedModule.addAll(client.condition().findLoadedModules());
    }
    loadedModule.addAll(server.condition().findLoadedModules());
    return loadedModule.stream().distinct().toList();
  }

  /**
   * Closes server and sends farewell greeting
   */
  public void disconnect() {
    server.broadcastPacket(new PacketOutgoingDisconnect());
    server.close();
  }
}
