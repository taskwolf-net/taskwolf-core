package net.taskwolf.core.distribution.server.node;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.netty.channel.Channel;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.DistributionConfiguration;
import net.taskwolf.core.distribution.client.DistributionClient;
import net.taskwolf.core.distribution.client.DistributionClientRegistry;
import net.taskwolf.core.distribution.client.packet.PacketOutgoingHandshakeResponse;
import net.taskwolf.core.distribution.packet.PacketRegistry;
import net.taskwolf.core.event.EventExecutor;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.core.event.node.NodeHandshakeRequestEvent;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class NodeHandshakeRequestHook implements Hook {
  private final DistributionConfiguration distributionConfiguration;
  private final PacketRegistry packetRegistry;
  private final EventExecutor eventExecutor;
  private final DistributionClientRegistry distributionClientRegistry;

  @EventHook
  private void nodeHandshake(NodeHandshakeRequestEvent event) {
    if (!event.key().equals(event.node().distributionKey())) {
      sendHandshakeResponsePacket(event.channel(), false);
      event.channel().close();
      return;
    }
    var client = DistributionClient.of(distributionConfiguration, packetRegistry,
      eventExecutor, distributionClientRegistry, event.node(), event.channel());
    distributionClientRegistry.registerClient(client);
    sendHandshakeResponsePacket(event.channel(), true);
  }

  private void sendHandshakeResponsePacket(Channel channel, boolean success) {
    channel.writeAndFlush(new PacketOutgoingHandshakeResponse(success));
  }
}
