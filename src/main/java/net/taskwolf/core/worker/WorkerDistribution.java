package net.taskwolf.core.worker;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventExecutor;
import net.taskwolf.core.event.HookRegistry;
import net.taskwolf.core.packet.PacketEventRepository;
import net.taskwolf.core.packet.PacketRegistry;
import net.taskwolf.core.worker.client.WorkerProxyClient;
import net.taskwolf.core.worker.event.node.NodeHandshakeResponseEvent;
import net.taskwolf.core.worker.event.node.NodePingEvent;
import net.taskwolf.core.worker.event.user.UsersReorganizeEvent;
import net.taskwolf.core.worker.packet.incoming.node.PacketIncomingHandshakeResponse;
import net.taskwolf.core.worker.packet.incoming.node.PacketIncomingPing;
import net.taskwolf.core.worker.packet.incoming.user.PacketIncomingUsersReorganize;
import net.taskwolf.core.worker.packet.outgoing.node.PacketOutgoingDisconnect;
import net.taskwolf.core.worker.packet.outgoing.node.PacketOutgoingHandshakeRequest;
import net.taskwolf.core.worker.packet.outgoing.node.PacketOutgoingModuleLoad;
import net.taskwolf.core.worker.packet.outgoing.node.PacketOutgoingModuleUnload;
import net.taskwolf.core.worker.server.WorkerServer;
import net.taskwolf.core.worker.server.node.NodeDisconnectHook;
import net.taskwolf.core.worker.server.node.NodeHandshakeResponseHook;
import net.taskwolf.core.worker.server.node.NodePingHook;
import net.taskwolf.core.worker.server.user.UsersReorganizeHook;

import java.util.List;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class WorkerDistribution {
  private final Injector injector;
  private final WorkerConfiguration configuration;
  private final PacketRegistry packetRegistry;
  private final EventExecutor eventExecutor;
  private final HookRegistry hookRegistry;
  private final WorkerUserAssignment userAssignment;
  private final PacketEventRepository packetEventRepository;
  private final WorkerProxyClient workerProxyClient;
  private WorkerServer workerServer;

  /**
   * Initializes node distribution (registers packets, hooks, events and
   * opens server)
   * @throws Exception
   */
  public void initialize() throws Exception {
    workerServer = WorkerServer.create(configuration, packetRegistry,
      eventExecutor, workerProxyClient, packetEventRepository,
      configuration.distributionPort());
    registerPackets();
    registerHooks();
    registerEvents();
    workerServer.openAsync(this::connectToProxy);
  }

  private void connectToProxy() {
    workerProxyClient.connectAsync(() -> workerProxyClient.sendPacket(
      new PacketOutgoingHandshakeRequest(configuration.distributionKey())));
  }

  private void registerPackets() throws Exception {
    packetRegistry.registerPacket(PacketIncomingHandshakeResponse.class);
    packetRegistry.registerPacket(PacketIncomingPing.class);
    packetRegistry.registerPacket(PacketIncomingUsersReorganize.class);
  }

  private void registerHooks() {
    hookRegistry.register(injector.getInstance(NodeDisconnectHook.class));
    hookRegistry.register(injector.getInstance(NodeHandshakeResponseHook.class));
    hookRegistry.register(injector.getInstance(NodePingHook.class));
    hookRegistry.register(injector.getInstance(UsersReorganizeHook.class));
  }

  private void registerEvents() {
    packetEventRepository.registerEvent(PacketIncomingHandshakeResponse.class,
      (client, packet) -> NodeHandshakeResponseEvent.create(packet.success()));
    packetEventRepository.<WorkerProxyClient, PacketIncomingPing>registerEvent(
      PacketIncomingPing.class, (client, packet) ->
        NodePingEvent.create(client, packet.value()));
    packetEventRepository.registerEvent(PacketIncomingUsersReorganize.class,
      (client, packet) -> UsersReorganizeEvent.create(packet.module(), packet.users()));
  }

  /**
   * Registers a new module
   * @param module The module name
   */
  public void registerModule(String module) {
    workerProxyClient.sendPacket(new PacketOutgoingModuleLoad(module));
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
   * Unregisters a module
   * @param module The name of the module
   */
  public void unregisterModule(String module) {
    workerProxyClient.sendPacket(new PacketOutgoingModuleUnload(module));
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
   * Closes server and sends farewell greeting
   */
  public void disconnect() {
    workerProxyClient.sendPacket(new PacketOutgoingDisconnect());
    workerServer.close();
  }
}
