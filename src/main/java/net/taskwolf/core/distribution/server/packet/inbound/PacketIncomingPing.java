package net.taskwolf.core.distribution.server.packet.inbound;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.packet.PacketBuffer;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingPing extends PacketIncoming {
  private int value;

  public PacketIncomingPing() {
    super(0x01);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    value = buffer.readVarInt();
  }
}
