package net.taskwolf.core.distribution.client.packet.outbound;

import net.taskwolf.core.distribution.packet.PacketBuffer;

public final class PacketOutgoingPing extends PacketOutgoing {
  private final int value;

  public PacketOutgoingPing(int value) {
    super(0x01);
    this.value = value;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeVarInt(value);
  }
}
