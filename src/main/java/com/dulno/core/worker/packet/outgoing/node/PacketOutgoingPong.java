package com.dulno.core.worker.packet.outgoing.node;

import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.outgoing.PacketOutgoing;

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
