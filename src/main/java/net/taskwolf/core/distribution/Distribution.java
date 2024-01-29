package net.taskwolf.core.distribution;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.client.DistributionClient;
import net.taskwolf.core.distribution.client.DistributionClientRegistry;
import net.taskwolf.core.distribution.client.packet.node.PacketOutgoingHandshakeRequest;
import net.taskwolf.core.distribution.packet.PacketRegistry;
import net.taskwolf.core.distribution.server.DistributionServer;
import net.taskwolf.core.event.EventExecutor;

import java.util.List;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class Distribution {
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

  }

  public void addUser(UUID user) {

  }

  public void removeUser(UUID user) {

  }

  public void unregisterModule(String module) {

  }

  public boolean isAssignedUser(String module, UUID user) {
    return false;
  }

  public List<UUID> findAssignedUsers(String module) {
    return null;
  }

  public List<String> findConnectedNodes() {
    return null;
  }

  public void disconnect() {

  }
}
