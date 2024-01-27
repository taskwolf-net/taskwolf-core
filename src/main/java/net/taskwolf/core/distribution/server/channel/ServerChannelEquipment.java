package net.taskwolf.core.distribution.server.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.DistributionConfiguration;
import net.taskwolf.core.distribution.server.packet.PacketDecoder;
import net.taskwolf.core.distribution.server.packet.PacketRegistry;

import java.net.InetSocketAddress;

@RequiredArgsConstructor(staticName = "create")
public final class ServerChannelEquipment extends ChannelInitializer<Channel> {
  private final DistributionConfiguration distributionConfiguration;
  private final PacketRegistry packetRegistry;

  @Override
  protected void initChannel(Channel channel) {
    var channelHost = ((InetSocketAddress) channel.remoteAddress())
      .getAddress().getHostAddress();
    var isAuthorized = distributionConfiguration.nodes().stream().anyMatch(
      node -> node.hostname().equals(channelHost));
    if (!isAuthorized) {
      channel.close();
      return;
    }
    channel.pipeline().addLast("channel-decoder", PacketDecoder.create(packetRegistry));
    channel.pipeline().addLast("chanel-inbox", ServerChannelInbox.create());
  }
}