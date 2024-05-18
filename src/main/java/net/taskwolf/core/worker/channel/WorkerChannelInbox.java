package net.taskwolf.core.worker.channel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventExecutor;
import net.taskwolf.core.packet.PacketEventRepository;
import net.taskwolf.core.worker.client.WorkerProxyClient;
import net.taskwolf.core.worker.event.node.NodeDisconnectEvent;
import net.taskwolf.core.worker.packet.incoming.PacketIncoming;

@RequiredArgsConstructor(staticName = "create")
public final class WorkerChannelInbox extends SimpleChannelInboundHandler<PacketIncoming> {
  private final EventExecutor eventExecutor;
  private final WorkerProxyClient workerProxyClient;
  private final PacketEventRepository packetEventRepository;

  @Override
  protected void channelRead0(
    ChannelHandlerContext context, PacketIncoming incomingPacket
  ) {
    if (context.channel() != workerProxyClient.channel()) {
      return;
    }
    var event = packetEventRepository.findEvent(incomingPacket.getClass());
    if (event.isEmpty()) {
      return;
    }
    eventExecutor.execute(event.get().process(workerProxyClient, incomingPacket));
  }

  @Override
  public void channelInactive(ChannelHandlerContext context) {
    if (context.channel() != workerProxyClient.channel()) {
      return;
    }
    eventExecutor.execute(NodeDisconnectEvent.create(
      NodeDisconnectEvent.DisconnectReason.TIME_OUT));
  }
}

