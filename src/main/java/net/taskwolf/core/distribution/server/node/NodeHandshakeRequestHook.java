package net.taskwolf.core.distribution.server.node;

import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.DistributionConfiguration;
import net.taskwolf.core.distribution.client.DistributionClient;
import net.taskwolf.core.distribution.client.DistributionClientRegistry;
import net.taskwolf.core.distribution.client.packet.node.PacketOutgoingHandshakeResponse;
import net.taskwolf.core.distribution.packet.PacketRegistry;
import net.taskwolf.core.distribution.server.DistributionServer;
import net.taskwolf.core.event.EventExecutor;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.core.event.node.NodeHandshakeRequestEvent;

@RequiredArgsConstructor(staticName = "create")
public final class NodeHandshakeRequestHook implements Hook {
  private final DistributionConfiguration distributionConfiguration;
  private final PacketRegistry packetRegistry;
  private final EventExecutor eventExecutor;
  private final DistributionClientRegistry distributionClientRegistry;
  private final DistributionServer server;

  @EventHook
  private void nodeHandshake(NodeHandshakeRequestEvent event) {
    if (!event.key().equals(event.node().distributionKey())) {
      event.channel().writeAndFlush(new PacketOutgoingHandshakeResponse(false));
      event.channel().close();
      return;
    }
    var client = DistributionClient.of(distributionConfiguration, packetRegistry,
      eventExecutor, distributionClientRegistry, event.nodeId(), event.node(),
      event.channel());
    distributionClientRegistry.registerClient(client);
    event.channel().writeAndFlush(new PacketOutgoingHandshakeResponse(true,
      server.nodeId()));
  }
}
