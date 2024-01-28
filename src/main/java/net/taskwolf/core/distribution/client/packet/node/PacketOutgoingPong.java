package net.taskwolf.core.distribution.client.packet.node;

import net.taskwolf.core.distribution.client.packet.PacketOutgoing;
import net.taskwolf.core.distribution.packet.PacketBuffer;

public final class PacketOutgoingPong extends PacketOutgoing {
  private final int value;

  public PacketOutgoingPong(int value) {
    super(0x03);
    this.value = value;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeVarInt(value);
  }
}
