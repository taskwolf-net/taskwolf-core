package net.taskwolf.core.distribution.channel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.DistributionConfiguration;
import net.taskwolf.core.distribution.Node;
import net.taskwolf.core.distribution.client.DistributionClient;
import net.taskwolf.core.distribution.client.DistributionClientRegistry;
import net.taskwolf.core.distribution.server.packet.*;
import net.taskwolf.core.event.EventExecutor;
import net.taskwolf.core.event.node.*;

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
      processHandshakeRequestPacket(context, packet);
    } else if (incomingPacket instanceof PacketIncomingHandshakeResponse packet) {
      processHandshakeResponsePacket(context, packet);
    } else if (incomingPacket instanceof PacketIncomingPing packet) {
      processPingPacket(context, packet);
    } else if (incomingPacket instanceof PacketIncomingPong packet) {
      processPongPacket(context, packet);
    }
  }

  private void processHandshakeRequestPacket(
    ChannelHandlerContext context, PacketIncomingHandshakeRequest packet
  ) {
    eventExecutor.execute(NodeHandshakeRequestEvent.create(findNodeByContext(
      packet.hostname(), packet.port()), context.channel(), packet.key()));
  }

  private void processHandshakeResponsePacket(
    ChannelHandlerContext context, PacketIncomingHandshakeResponse packet
  ) {
    eventExecutor.execute(NodeHandshakeResponseEvent.create(
      findClientByContext(context), packet.success()));
  }

  private void processPingPacket(
    ChannelHandlerContext context, PacketIncomingPing packet
  ) {
    eventExecutor.execute(NodePingEvent.create(
      findClientByContext(context), packet.value()));
  }

  private void processPongPacket(
    ChannelHandlerContext context, PacketIncomingPong packet
  ) {
    eventExecutor.execute(NodePongEvent.create(
      findClientByContext(context), packet.value()));
  }

  @Override
  public void channelInactive(ChannelHandlerContext context) {
    eventExecutor.execute(NodeDisconnectEvent.create(
      findClientByContext(context)));
  }

  private Node findNodeByContext(String hostname, int port) {
    return distributionConfiguration.nodes().stream().filter(entry ->
        entry.hostname().equals(hostname) && entry.distributionPort() == port)
      .findFirst().get();
  }

  private DistributionClient findClientByContext(ChannelHandlerContext context) {
    var channel = context.channel();
    return distributionClientRegistry.findAllClients().stream()
      .filter(client -> client.channel().equals(channel))
      .findFirst().get();
  }
}

