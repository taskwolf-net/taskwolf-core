package com.dulno.core.worker.packet.outgoing.node;

import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.outgoing.PacketOutgoing;

public final class PacketOutgoingHandshakeRequest extends PacketOutgoing {
  private final String key;

  public PacketOutgoingHandshakeRequest(String key) {
    super(0x00);
    this.key = key;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeString(key);
  }
}
