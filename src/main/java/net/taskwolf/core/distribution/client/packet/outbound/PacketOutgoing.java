package net.taskwolf.core.distribution.client.packet.outbound;

import net.taskwolf.core.distribution.packet.Packet;
import net.taskwolf.core.distribution.packet.PacketBuffer;

public abstract class PacketOutgoing extends Packet {
  protected PacketOutgoing(int id) {
    super(id);
  }

  public abstract void write(PacketBuffer buffer) throws Exception;
}
