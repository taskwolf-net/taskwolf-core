package net.taskwolf.core.distribution.channel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.DistributionConfiguration;
import net.taskwolf.core.distribution.Node;
import net.taskwolf.core.distribution.client.DistributionClient;
import net.taskwolf.core.distribution.client.DistributionClientRegistry;
import net.taskwolf.core.distribution.server.packet.PacketIncoming;
import net.taskwolf.core.distribution.server.packet.node.*;
import net.taskwolf.core.distribution.server.packet.user.PacketIncomingUserDelete;
import net.taskwolf.core.distribution.server.packet.user.PacketIncomingUsersReorganize;
import net.taskwolf.core.event.EventExecutor;
import net.taskwolf.core.event.node.*;
import net.taskwolf.core.event.user.UserDeleteEvent;
import net.taskwolf.core.event.user.UsersReorganizeEvent;

import java.util.Optional;

@RequiredArgsConstructor(staticName = "create")
public final class ChannelInbox extends SimpleChannelInboundHandler<PacketIncoming> {
  private final EventExecutor eventExecutor;
  private final DistributionConfiguration distributionConfiguration;
  private final DistributionClientRegistry distributionClientRegistry;

  @Override
  protected void channelRead0(
    ChannelHandlerContext context, PacketIncoming incomingPacket
  ) {
    var client = findClientByContext(context);
    if (client.isEmpty() || client.get().state().isUnauthorized()) {
      processUnauthorizedChannel(context, client, incomingPacket);
      return;
    }
    processAuthorizedChannel(client.get(), incomingPacket);
  }

  private void processUnauthorizedChannel(
    ChannelHandlerContext context, Optional<DistributionClient> client,
    PacketIncoming incomingPacket
  ) {
    if (incomingPacket instanceof PacketIncomingHandshakeRequest packet) {
      processHandshakeRequestPacket(context, packet);
    } else if (incomingPacket instanceof PacketIncomingHandshakeResponse packet) {
        client.ifPresent(distributionClient ->
          processHandshakeResponsePacket(distributionClient, packet));
    }
  }

  private void processHandshakeRequestPacket(
    ChannelHandlerContext context, PacketIncomingHandshakeRequest packet
  ) {
    eventExecutor.execute(NodeHandshakeRequestEvent.create(findNodeByContext(
      packet.hostname(), packet.port()), context.channel(), packet.key(),
      packet.nodeId()));
  }

  private void processAuthorizedChannel(
    DistributionClient client, PacketIncoming incomingPacket
  ) {
    if (incomingPacket instanceof PacketIncomingPing packet) {
      processPingPacket(client, packet);
    } else if (incomingPacket instanceof PacketIncomingPong packet) {
      processPongPacket(client, packet);
    } else if (incomingPacket instanceof PacketIncomingDisconnect packet) {
      processDisconnectPacket(client, packet);
    } else if (incomingPacket instanceof PacketIncomingModuleLoad packet) {
      processModuleLoadPacket(client, packet);
    } else if (incomingPacket instanceof PacketIncomingModuleUnload packet) {
      processModuleUnloadPacket(client, packet);
    } else if (incomingPacket instanceof PacketIncomingUsersReorganize packet) {
      processUsersReorganizePacket(packet);
    } else if (incomingPacket instanceof PacketIncomingUserDelete packet) {
      processUserDeletePacket(packet);
    }
  }

  private void processHandshakeResponsePacket(
    DistributionClient client, PacketIncomingHandshakeResponse packet
  ) {
    if (packet.success()) {
      eventExecutor.execute(NodeHandshakeResponseEvent.create(client,
        true, packet.nodeId(), packet.loadedModules()));
      return;
    }
    eventExecutor.execute(NodeHandshakeResponseEvent.create(client, false));
  }

  private void processPingPacket(
    DistributionClient client, PacketIncomingPing packet
  ) {
    eventExecutor.execute(NodePingEvent.create(client, packet.value()));
  }

  private void processPongPacket(
    DistributionClient client, PacketIncomingPong packet
  ) {
    eventExecutor.execute(NodePongEvent.create(client, packet.value()));
  }

  private void processDisconnectPacket(
    DistributionClient client, PacketIncomingDisconnect packet
  ) {
    eventExecutor.execute(NodeDisconnectEvent.create(client,
      NodeDisconnectEvent.DisconnectReason.SHUTDOWN));
  }

  private void processModuleLoadPacket(
    DistributionClient client, PacketIncomingModuleLoad packet
  ) {
    eventExecutor.execute(NodeModuleLoadEvent.create(client, packet.module()));
  }

  private void processModuleUnloadPacket(
    DistributionClient client, PacketIncomingModuleUnload packet
  ) {
    eventExecutor.execute(NodeModuleUnloadEvent.create(client, packet.module()));
  }

  private void processUsersReorganizePacket(PacketIncomingUsersReorganize packet) {
    eventExecutor.execute(UsersReorganizeEvent.create(packet.module(),
      packet.users()));
  }

  private void processUserDeletePacket(PacketIncomingUserDelete packet) {
    eventExecutor.execute(UserDeleteEvent.create(packet.user()));
  }

  @Override
  public void channelInactive(ChannelHandlerContext context) {
    var client = findClientByContext(context);
    if (client.isEmpty()) {
      return;
    }
    eventExecutor.execute(NodeDisconnectEvent.create(client.get(),
      NodeDisconnectEvent.DisconnectReason.TIME_OUT));
  }

  private Node findNodeByContext(String hostname, int port) {
    return distributionConfiguration.nodes().stream().filter(entry ->
        entry.hostname().equals(hostname) && entry.distributionPort() == port)
      .findFirst().get();
  }

  private Optional<DistributionClient> findClientByContext(ChannelHandlerContext context) {
    var channel = context.channel();
    return distributionClientRegistry.findAllClients().stream()
      .filter(client -> client.channel().equals(channel))
      .findFirst();
  }
}

