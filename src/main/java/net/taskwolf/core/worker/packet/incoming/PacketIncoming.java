package net.taskwolf.core.worker.packet.incoming;

import net.taskwolf.core.packet.Packet;
import net.taskwolf.core.packet.PacketBuffer;

public abstract class PacketIncoming extends Packet {
  protected PacketIncoming(int id) {
    super(id);
  }

  /**
   * Deserializes an incoming packet
   * @param buffer The buffer from which the information is read out
   * @throws Exception
   */
  public abstract void read(PacketBuffer buffer) throws Exception;
}
