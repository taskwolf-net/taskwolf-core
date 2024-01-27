package net.taskwolf.core.distribution.client.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.client.packet.outbound.PacketOutgoing;
import net.taskwolf.core.distribution.packet.PacketBuffer;

@RequiredArgsConstructor(staticName = "create")
public class PacketEncoder extends MessageToByteEncoder<PacketOutgoing> {
  @Override
  protected void encode(
    ChannelHandlerContext context, PacketOutgoing packet, ByteBuf byteBuf
  ) throws Exception {
    var buffer = PacketBuffer.create(Unpooled.buffer(0));
    buffer.writeVarInt(packet.id());
    packet.write(buffer);
    byteBuf.writeBytes(buffer.raw());
  }
}
