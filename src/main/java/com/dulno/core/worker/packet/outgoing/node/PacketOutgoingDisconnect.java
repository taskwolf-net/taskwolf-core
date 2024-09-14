package com.dulno.core.worker.packet.outgoing.node;

import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.outgoing.PacketOutgoing;

public final class PacketOutgoingDisconnect extends PacketOutgoing {
  public PacketOutgoingDisconnect() {
    super(0x04);
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {

  }
}

