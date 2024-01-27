package net.taskwolf.core.distribution.server.packet;

import net.taskwolf.core.distribution.packet.Packet;
import net.taskwolf.core.distribution.packet.PacketBuffer;

public abstract class PacketIncoming extends Packet {
  protected PacketIncoming(int id) {
    super(id);
  }

  public abstract void read(PacketBuffer buffer) throws Exception;
}
