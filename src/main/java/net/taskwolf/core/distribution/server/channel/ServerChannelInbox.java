package net.taskwolf.core.distribution.server.channel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.server.packet.inbound.PacketIncoming;
import net.taskwolf.core.event.EventExecutor;

@RequiredArgsConstructor(staticName = "create")
public final class ServerChannelInbox extends SimpleChannelInboundHandler<PacketIncoming> {
  private final EventExecutor eventHook;

  @Override
  protected void channelRead0(
    ChannelHandlerContext context, PacketIncoming incomingPacket
  ) {

  }

  @Override
  public void channelInactive(ChannelHandlerContext context) throws Exception {

  }
}

