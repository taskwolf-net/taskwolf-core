package net.taskwolf.core.distribution.client.packet;

import net.taskwolf.core.distribution.packet.Packet;
import net.taskwolf.core.distribution.packet.PacketBuffer;

public abstract class PacketOutgoing extends Packet {
  protected PacketOutgoing(int id) {
    super(id);
  }

  /**
   * Serializes an outgoing packet
   * @param buffer The buffer where the information is written to
   * @throws Exception
   */
  public abstract void write(PacketBuffer buffer) throws Exception;
}
