package net.taskwolf.core.distribution.channel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.DistributionConfiguration;
import net.taskwolf.core.distribution.client.DistributionClient;
import net.taskwolf.core.distribution.client.DistributionClientRegistry;
import net.taskwolf.core.distribution.server.packet.PacketIncoming;
import net.taskwolf.core.distribution.server.packet.PacketIncomingHandshakeRequest;
import net.taskwolf.core.distribution.server.packet.PacketIncomingPing;
import net.taskwolf.core.event.EventExecutor;
import net.taskwolf.core.event.node.NodeDisconnectEvent;
import net.taskwolf.core.event.node.NodeHandshakeRequestEvent;
import net.taskwolf.core.event.node.NodePingEvent;

@RequiredArgsConstructor(staticName = "create")
public final class ChannelInbox extends SimpleChannelInboundHandler<PacketIncoming> {
  private final EventExecutor eventExecutor;
  private final DistributionConfiguration distributionConfiguration;
  private final DistributionClientRegistry distributionClientRegistry;

  @Override
  protected void channelRead0(
    ChannelHandlerContext context, PacketIncoming incomingPacket
  ) {
    if (incomingPacket instanceof PacketIncomingHandshakeRequest packet) {
      processHandshakePacket(context, packet);
    } else if (incomingPacket instanceof PacketIncomingPing packet) {
      processPingPacket(context, packet);
    }
  }

  private void processHandshakePacket(
    ChannelHandlerContext context, PacketIncomingHandshakeRequest packet
  ) {
    var node = distributionConfiguration.nodes().stream().filter(entry ->
      entry.hostname().equals(packet.hostname()) &&
        entry.distributionPort() == packet.port()).findFirst().get();
    eventExecutor.execute(NodeHandshakeRequestEvent.create(node, context.channel(),
      packet.key()));
  }

  private void processPingPacket(
    ChannelHandlerContext context, PacketIncomingPing packet
  ) {
    eventExecutor.execute(NodePingEvent.create(findNodeByContext(context),
      packet.value()));
  }

  @Override
  public void channelInactive(ChannelHandlerContext context) {
    eventExecutor.execute(NodeDisconnectEvent.create(findNodeByContext(context)));
  }

  private DistributionClient findNodeByContext(ChannelHandlerContext context) {
    var channel = context.channel();
    return distributionClientRegistry.findAllClients().stream()
      .filter(client -> client.channel().equals(channel))
      .findFirst().get();
  }
}

