package net.taskwolf.core.worker.client;

import net.taskwolf.core.packet.PacketRegistry;
import net.taskwolf.core.worker.WorkerConfiguration;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.event.EventExecutor;
import net.taskwolf.core.packet.PacketEventRepository;
import net.taskwolf.core.worker.channel.WorkerChannelEquipment;
import net.taskwolf.core.worker.event.node.NodeDisconnectEvent;
import net.taskwolf.core.worker.packet.outgoing.PacketOutgoing;

@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class WorkerProxyClient {
  public static WorkerProxyClient of(
          WorkerConfiguration configuration, PacketRegistry packetRegistry,
          EventExecutor eventExecutor, PacketEventRepository packetEventRepository,
          String hostname, int port, Channel channel
  ) {
    return new WorkerProxyClient(configuration, packetRegistry, eventExecutor,
      packetEventRepository, hostname, port, channel);
  }

  private final WorkerConfiguration configuration;
  private final PacketRegistry packetRegistry;
  private final EventExecutor eventExecutor;
  private final PacketEventRepository packetEventRepository;
  @Getter
  private final String hostname;
  @Getter
  private final int port;
  @Getter
  private Channel channel;
  private EventLoopGroup group;

  private WorkerProxyClient(
    WorkerConfiguration configuration, PacketRegistry packetRegistry,
    EventExecutor eventExecutor, PacketEventRepository packetEventRepository,
    String hostname, int port, Channel channel
  ) {
    this.configuration = configuration;
    this.packetRegistry = packetRegistry;
    this.eventExecutor = eventExecutor;
    this.packetEventRepository = packetEventRepository;
    this.hostname = hostname;
    this.port = port;
    this.channel = channel;
  }

  /**
   * Connects the client to the server in the background
   * @param callback A future that is called when the connection process is completed
   */
  public void connectAsync(Runnable callback) {
    new Thread(() -> connect(callback)).start();
  }

  private void connect(Runnable callback) {
    connect();
    callback.run();
  }

  /**
   * Connects the client to the server
   */
  public void connect() {
    try {
      group = new NioEventLoopGroup();
      channel = new Bootstrap()
        .group(group)
        .channel(NioSocketChannel.class)
        .handler(WorkerChannelEquipment.create(configuration, packetRegistry,
          eventExecutor, this, packetEventRepository,
          WorkerChannelEquipment.Type.INTERNAL))
        .connect(hostname, port)
        .syncUninterruptibly().channel();
    } catch (Exception exception) {
      eventExecutor.execute(NodeDisconnectEvent.create(
        NodeDisconnectEvent.DisconnectReason.CONNECTION_FAILED));
    }
  }

  /**
   * Sends a packet to the server
   * @param packet The packet that is to be send
   * @param <T> The generic type of the packet
   */
  public <T extends PacketOutgoing> void sendPacket(T packet) {
    if (channel == null) {
      return;
    }
    channel.writeAndFlush(packet);
  }

  /**
   * Disconnects the client from the server
   */
  public void disconnect() {
    group.shutdownGracefully();
    channel.close();
  }
}
