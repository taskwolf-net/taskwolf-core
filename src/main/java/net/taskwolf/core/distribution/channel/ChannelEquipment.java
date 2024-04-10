package net.taskwolf.core.distribution.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.DistributionConfiguration;
import net.taskwolf.core.distribution.client.DistributionClientRegistry;
import net.taskwolf.core.distribution.packet.PacketDecoder;
import net.taskwolf.core.distribution.packet.PacketEncoder;
import net.taskwolf.core.distribution.packet.PacketEventRepository;
import net.taskwolf.core.distribution.packet.PacketRegistry;
import net.taskwolf.core.event.EventExecutor;

import java.net.InetSocketAddress;

@RequiredArgsConstructor(staticName = "create")
public final class ChannelEquipment extends ChannelInitializer<Channel> {
  public enum Type {
    EXTERNAL,
    INTERNAL;

    public boolean isExternal() {
      return this == EXTERNAL;
    }

    public boolean isInternal() {
      return this == INTERNAL;
    }
  }

  private final DistributionConfiguration distributionConfiguration;
  private final PacketRegistry packetRegistry;
  private final EventExecutor eventExecutor;
  private final DistributionClientRegistry distributionClientRegistry;
  private final PacketEventRepository packetEventRepository;
  private final Type type;

  @Override
  protected void initChannel(Channel channel) {
    if (type.isInternal()) {
      equipChannel(channel);
      return;
    }
    if (channel.remoteAddress() == null) {
      channel.close();
      return;
    }
    checkChannelAuthorization(channel);
  }

  private void checkChannelAuthorization(Channel channel) {
    var channelHost = ((InetSocketAddress) channel.remoteAddress())
      .getAddress().getHostAddress();
    var isAuthorized = distributionConfiguration.nodes().stream().anyMatch(
      node -> node.hostname().equals(channelHost));
    if (!isAuthorized) {
      channel.close();
      return;
    }
    equipChannel(channel);
  }

  private void equipChannel(Channel channel) {
    channel.pipeline().addLast("channel-encoder", PacketEncoder.create());
    channel.pipeline().addLast("channel-decoder", PacketDecoder.create(
      packetRegistry));
    channel.pipeline().addLast("chanel-inbox", ChannelInbox.create(eventExecutor,
      distributionConfiguration, distributionClientRegistry, packetEventRepository));
  }
}