package net.taskwolf.core.distribution.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.DistributionConfiguration;
import net.taskwolf.core.distribution.DistributionNodeCondition;
import net.taskwolf.core.distribution.Node;
import net.taskwolf.core.distribution.channel.ChannelEquipment;
import net.taskwolf.core.distribution.client.packet.PacketOutgoing;
import net.taskwolf.core.distribution.packet.PacketEventRepository;
import net.taskwolf.core.distribution.packet.PacketRegistry;
import net.taskwolf.core.event.EventExecutor;
import net.taskwolf.core.event.node.NodeDisconnectEvent;

import java.util.UUID;

@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class DistributionClient {
  public static DistributionClient of(
    DistributionConfiguration distributionConfiguration,
    PacketRegistry packetRegistry, EventExecutor eventExecutor,
    DistributionClientRegistry clientRegistry,
    PacketEventRepository packetEventRepository, UUID nodeId, Node node,
    Channel channel
  ) {
    return new DistributionClient(distributionConfiguration, packetRegistry,
      eventExecutor, clientRegistry, packetEventRepository, nodeId, node, channel);
  }

  private final DistributionConfiguration distributionConfiguration;
  private final PacketRegistry packetRegistry;
  private final EventExecutor eventExecutor;
  private final DistributionClientRegistry clientRegistry;
  private final PacketEventRepository packetEventRepository;
  @Getter
  private UUID nodeId;
  @Getter
  private final Node node;
  @Getter
  private DistributionClientState state = DistributionClientState.UNAUTHORIZED;
  @Getter
  private final DistributionNodeCondition condition =
    DistributionNodeCondition.create();
  @Getter
  private Channel channel;
  private EventLoopGroup group;

  private DistributionClient(
    DistributionConfiguration distributionConfiguration,
    PacketRegistry packetRegistry, EventExecutor eventExecutor,
    DistributionClientRegistry clientRegistry,
    PacketEventRepository packetEventRepository, UUID nodeId, Node node,
    Channel channel
  ) {
    this.distributionConfiguration = distributionConfiguration;
    this.packetRegistry = packetRegistry;
    this.eventExecutor = eventExecutor;
    this.clientRegistry = clientRegistry;
    this.packetEventRepository = packetEventRepository;
    this.nodeId = nodeId;
    this.node = node;
    this.channel = channel;
  }

  public void connectAsync(Runnable callback) {
    new Thread(() -> connect(callback)).start();
  }

  private void connect(Runnable callback) {
    connect();
    callback.run();
  }

  public void connect() {
    try {
      group = new NioEventLoopGroup();
      channel = new Bootstrap()
        .group(group)
        .channel(NioSocketChannel.class)
        .handler(ChannelEquipment.create(distributionConfiguration,
          packetRegistry, eventExecutor, clientRegistry,
          packetEventRepository, ChannelEquipment.Type.INTERNAL))
        .connect(node.hostname(), node.distributionPort())
        .syncUninterruptibly().channel();
    } catch (Exception exception) {
      eventExecutor.execute(NodeDisconnectEvent.create(this,
        NodeDisconnectEvent.DisconnectReason.CONNECTION_FAILED));
    }
  }

  public <T extends PacketOutgoing> void sendPacket(T packet) {
    if (channel == null) {
      return;
    }
    channel.writeAndFlush(packet);
  }

  public void disconnect() {
    group.shutdownGracefully();
    channel.close();
  }

  public void updateNodeId(UUID newNodeId) {
    nodeId = newNodeId;
  }

  public void authorize() {
    state = DistributionClientState.AUTHORIZED;
  }
}
