package com.dulno.core.worker;

import com.dulno.core.event.HookRegistry;
import com.dulno.core.packet.PacketEventRepository;
import com.dulno.core.packet.PacketRegistry;
import com.dulno.core.worker.client.WorkerProxyClient;
import com.dulno.core.worker.event.node.NodeHandshakeResponseEvent;
import com.dulno.core.worker.event.node.NodePingEvent;
import com.dulno.core.worker.event.user.UsersReorganizeEvent;
import com.dulno.core.worker.packet.outgoing.node.PacketOutgoingModulesLoad;
import com.dulno.core.worker.packet.outgoing.node.PacketOutgoingModulesUnload;
import com.dulno.core.worker.server.database.TableTransformHook;
import com.dulno.core.worker.server.node.NodeDisconnectHook;
import com.dulno.core.worker.server.node.NodeHandshakeResponseHook;
import com.dulno.core.worker.server.node.NodePingHook;
import com.dulno.core.worker.server.user.UsersReorganizeHook;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import com.dulno.core.worker.event.database.TableStateRequestEvent;
import com.dulno.core.worker.event.database.TableStateResponseEvent;
import com.dulno.core.worker.event.database.TableTransformEvent;
import com.dulno.core.worker.packet.incoming.database.PacketIncomingTableStateRequest;
import com.dulno.core.worker.packet.incoming.database.PacketIncomingTableStateResponse;
import com.dulno.core.worker.packet.incoming.database.PacketIncomingTableTransform;
import com.dulno.core.worker.packet.incoming.node.PacketIncomingHandshakeResponse;
import com.dulno.core.worker.packet.incoming.node.PacketIncomingPing;
import com.dulno.core.worker.packet.incoming.user.PacketIncomingUsersReorganize;
import com.dulno.core.worker.packet.outgoing.node.PacketOutgoingDisconnect;
import com.dulno.core.worker.packet.outgoing.node.PacketOutgoingHandshakeRequest;
import com.dulno.core.worker.server.database.TableStateRequestHook;
import com.dulno.core.worker.server.database.TableStateResponseHook;

import java.util.List;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class WorkerDistribution {
  private final Injector injector;
  private final WorkerConfiguration configuration;
  private final PacketRegistry packetRegistry;
  private final HookRegistry hookRegistry;
  private final WorkerUserAssignment userAssignment;
  private final PacketEventRepository packetEventRepository;
  private final WorkerProxyClient workerProxyClient;

  /**
   * Initializes node distribution (registers packets, hooks, events and
   * opens server)
   * @throws Exception
   */
  public void initialize() throws Exception {
    registerPackets();
    registerHooks();
    registerEvents();
    workerProxyClient.connectAsync(() -> workerProxyClient.sendPacket(
      new PacketOutgoingHandshakeRequest(configuration.distributionKey())));
  }

  private void registerPackets() throws Exception {
    packetRegistry.registerPacket(PacketIncomingHandshakeResponse.class);
    packetRegistry.registerPacket(PacketIncomingPing.class);
    packetRegistry.registerPacket(PacketIncomingUsersReorganize.class);
    packetRegistry.registerPacket(PacketIncomingTableTransform.class);
    packetRegistry.registerPacket(PacketIncomingTableStateRequest.class);
    packetRegistry.registerPacket(PacketIncomingTableStateResponse.class);
  }

  private void registerHooks() {
    hookRegistry.register(injector.getInstance(NodeDisconnectHook.class));
    hookRegistry.register(injector.getInstance(NodeHandshakeResponseHook.class));
    hookRegistry.register(injector.getInstance(NodePingHook.class));
    hookRegistry.register(injector.getInstance(UsersReorganizeHook.class));
    hookRegistry.register(injector.getInstance(TableTransformHook.class));
    hookRegistry.register(injector.getInstance(TableStateRequestHook.class));
    hookRegistry.register(injector.getInstance(TableStateResponseHook.class));
  }

  private void registerEvents() {
    packetEventRepository.registerEvent(PacketIncomingHandshakeResponse.class,
      (client, packet) -> NodeHandshakeResponseEvent.create(packet.success()));
    packetEventRepository.<WorkerProxyClient, PacketIncomingPing>registerEvent(
      PacketIncomingPing.class, (client, packet) ->
        NodePingEvent.create(client, packet.value()));
    packetEventRepository.registerEvent(PacketIncomingUsersReorganize.class,
      (client, packet) -> UsersReorganizeEvent.create(packet.module(), packet.users()));
    packetEventRepository.registerEvent(PacketIncomingTableTransform.class,
      (client, packet) -> TableTransformEvent.create(packet.tableClass()));
    packetEventRepository.registerEvent(PacketIncomingTableStateRequest.class,
      (client, packet) -> TableStateRequestEvent.create(packet.tableClass(),
        packet.state()));
    packetEventRepository.registerEvent(PacketIncomingTableStateResponse.class,
      (client, packet) -> TableStateResponseEvent.create(packet.tableClass(),
        packet.state()));
  }

  /**
   * Registers new modules
   * @param modules The name of the modules
   */
  public void registerMultipleModules(List<String> modules) {
    workerProxyClient.sendPacket(new PacketOutgoingModulesLoad(modules));
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
   * Unregisters modules
   * @param modules The name of the modules
   */
  public void unregisterMultipleModules(List<String> modules) {
    workerProxyClient.sendPacket(new PacketOutgoingModulesUnload(modules));
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
   * Sends farewell greeting
   */
  public void disconnect() {
    workerProxyClient.sendPacket(new PacketOutgoingDisconnect());
  }
}
