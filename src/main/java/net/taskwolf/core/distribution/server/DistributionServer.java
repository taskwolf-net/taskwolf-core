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
import net.taskwolf.core.distribution.DistributionNodeCondition;
import net.taskwolf.core.distribution.channel.ChannelEquipment;
import net.taskwolf.core.distribution.client.DistributionClientRegistry;
import net.taskwolf.core.distribution.client.packet.PacketOutgoing;
import net.taskwolf.core.distribution.packet.PacketEventRepository;
import net.taskwolf.core.distribution.packet.PacketRegistry;
import net.taskwolf.core.event.EventExecutor;

import java.util.UUID;

@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class DistributionServer {
  private final DistributionConfiguration distributionConfiguration;
  private final PacketRegistry packetRegistry;
  private final EventExecutor eventExecutor;
  private final DistributionClientRegistry clientRegistry;
  private final PacketEventRepository packetEventRepository;
  @Getter
  private final UUID nodeId;
  @Getter
  private final int port;
  @Getter
  private final DistributionNodeCondition condition =
    DistributionNodeCondition.create();
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
      .childHandler(ChannelEquipment.create(distributionConfiguration,
        packetRegistry, eventExecutor, clientRegistry, packetEventRepository,
        ChannelEquipment.Type.EXTERNAL))
      .bind(port)
      .syncUninterruptibly().channel();
  }

  /**
   * Sends an outgoing packet to all registered clients
   * @param packet The packet that will be sent
   */
  public void broadcastPacket(PacketOutgoing packet) {
    for (var client : clientRegistry.findAllClients()) {
      client.sendPacket(packet);
    }
  }

  /**
   * Closes the server
   */
  public void close() {
    group.shutdownGracefully();
    channel.close();
  }
}
