package net.taskwolf.core.distribution.server.packet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.packet.PacketBuffer;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public final class PacketDecoder extends ByteToMessageDecoder {
  private final PacketRegistry packetRegistry;

  @Override
  protected void decode(
    ChannelHandlerContext context, ByteBuf byteBuf, List<Object> list
  ) {
    try {
      var buffer = PacketBuffer.create(byteBuf);
      int id = buffer.readVarInt();
      var packetClass = packetRegistry.findPacket(id);
      if (packetClass.isEmpty()) {
        buffer.raw().skipBytes(buffer.raw().readableBytes());
        return;
      }
      var packet = packetClass.get().getConstructor().newInstance();
      packet.read(buffer);
      list.add(packet);
    } catch (Exception exception) {
    }
  }
}

