package net.taskwolf.core.worker.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.event.EventExecutor;
import net.taskwolf.core.packet.PacketEventRepository;
import net.taskwolf.core.packet.PacketRegistry;
import net.taskwolf.core.worker.WorkerConfiguration;
import net.taskwolf.core.worker.channel.WorkerChannelEquipment;
import net.taskwolf.core.worker.client.WorkerProxyClient;

@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class WorkerServer {
  private final WorkerConfiguration configuration;
  private final PacketRegistry packetRegistry;
  private final EventExecutor eventExecutor;
  private final WorkerProxyClient workerProxyClient;
  private final PacketEventRepository packetEventRepository;
  @Getter
  private final int port;
  @Getter
  private Channel channel;
  private EventLoopGroup group;

  /**
   * Opens the distribution server in the background
   * @param callback A future that is called when the opening process is completed
   */
  public void openAsync(Runnable callback) {
    new Thread(() -> open(callback)).start();
  }

  private void open(Runnable callback) {
    open();
    callback.run();
  }

  /**
   * Opens the distribution server
   */
  public void open() {
    group = new NioEventLoopGroup();
    channel = new ServerBootstrap()
      .group(group)
      .channel(NioServerSocketChannel.class)
      .childHandler(WorkerChannelEquipment.create(configuration,
        packetRegistry, eventExecutor, workerProxyClient, packetEventRepository,
        WorkerChannelEquipment.Type.EXTERNAL))
      .bind(port)
      .syncUninterruptibly().channel();
  }

  /**
   * Closes the server
   */
  public void close() {
    group.shutdownGracefully();
    channel.close();
  }
}
