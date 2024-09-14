package com.dulno.core.worker.packet.outgoing;

import com.dulno.core.packet.Packet;
import com.dulno.core.packet.PacketBuffer;

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
