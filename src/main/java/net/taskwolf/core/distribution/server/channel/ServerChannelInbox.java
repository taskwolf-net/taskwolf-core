package net.taskwolf.core.distribution.server.channel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.server.packet.inbound.PacketIncoming;

@RequiredArgsConstructor(staticName = "create")
public final class ServerChannelInbox extends SimpleChannelInboundHandler<PacketIncoming> {
  @Override
  protected void channelRead0(
    ChannelHandlerContext context, PacketIncoming incomingPacket
  ) {

  }

  @Override
  public void channelInactive(ChannelHandlerContext context) throws Exception {

  }
}

