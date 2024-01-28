package net.taskwolf.core.distribution.client.packet;

import net.taskwolf.core.distribution.packet.PacketBuffer;

public final class PacketOutgoingDisconnect extends PacketOutgoing {
  public PacketOutgoingDisconnect() {
    super(0x04);
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {

  }
}

