package net.taskwolf.core.worker.packet.outgoing.node;

import net.taskwolf.core.packet.PacketBuffer;
import net.taskwolf.core.worker.packet.outgoing.PacketOutgoing;

public final class PacketOutgoingDisconnect extends PacketOutgoing {
  public PacketOutgoingDisconnect() {
    super(0x04);
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {

  }
}

