package net.taskwolf.core.distribution.server.packet;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.packet.PacketBuffer;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingPong extends PacketIncoming {
  private int value;

  public PacketIncomingPong() {
    super(0x03);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    value = buffer.readVarInt();
  }
}
