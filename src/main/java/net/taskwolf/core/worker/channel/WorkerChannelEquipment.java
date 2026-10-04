package net.taskwolf.core.worker.channel;

import net.taskwolf.core.packet.PacketDecoder;
import net.taskwolf.core.packet.PacketEncoder;
import net.taskwolf.core.packet.PacketRegistry;
import net.taskwolf.core.worker.WorkerConfiguration;
import net.taskwolf.core.worker.client.WorkerProxyClient;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventExecutor;
import net.taskwolf.core.packet.PacketEventRepository;

import java.net.InetSocketAddress;

@RequiredArgsConstructor(staticName = "create")
public final class WorkerChannelEquipment extends ChannelInitializer<Channel> {
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

  private final WorkerConfiguration workerConfiguration;
  private final PacketRegistry packetRegistry;
  private final EventExecutor eventExecutor;
  private final WorkerProxyClient workerProxyClient;
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
    var isAuthorized = workerConfiguration.proxyHostname().equals(channelHost);
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
    channel.pipeline().addLast("chanel-inbox", WorkerChannelInbox.create(
      eventExecutor, workerProxyClient, packetEventRepository));
  }
}