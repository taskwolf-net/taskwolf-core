package net.taskwolf.core.distribution.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.Node;
import net.taskwolf.core.distribution.client.channel.ClientChannelEquipment;
import net.taskwolf.core.distribution.client.packet.outbound.PacketOutgoing;

@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class DistributionClient {
  @Getter
  private final Node node;
  @Getter
  private Channel channel;
  private EventLoopGroup group;

  public void connectAsync() {
    new Thread(this::connect).start();
  }

  public void connect() {
    group = new NioEventLoopGroup();
    channel = new Bootstrap()
      .group(group)
      .channel(NioSocketChannel.class)
      .handler(ClientChannelEquipment.create())
      .connect(node.hostname(), node.distributionPort())
      .syncUninterruptibly().channel();
  }

  public <T extends PacketOutgoing> void sendPacket(T packet) {
    channel.writeAndFlush(packet);
  }

  public void disconnect() {
    group.shutdownGracefully();
    channel.close();
  }
}
