package net.taskwolf.core.distribution.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.DistributionConfiguration;
import net.taskwolf.core.distribution.server.channel.ServerChannelEquipment;
import net.taskwolf.core.distribution.server.packet.PacketRegistry;
import net.taskwolf.core.event.EventExecutor;

@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class DistributionServer {
  private final DistributionConfiguration distributionConfiguration;
  private final PacketRegistry packetRegistry;
  private final EventExecutor eventExecutor;
  @Getter
  private final int port;
  @Getter
  private Channel channel;
  private EventLoopGroup group;

  public void openAsync(Runnable callback) {
    new Thread(() -> open(callback)).start();
  }

  private void open(Runnable callback) {
    open();
    callback.run();
  }

  public void open() {
    group = new NioEventLoopGroup();
    channel = new ServerBootstrap()
      .group(group)
      .channel(NioServerSocketChannel.class)
      .childHandler(ServerChannelEquipment.create(distributionConfiguration,
        packetRegistry, eventExecutor))
      .bind(port)
      .syncUninterruptibly().channel();
  }

  public void close() {
    group.shutdownGracefully();
    channel.close();
  }
}
