package net.taskwolf.core.worker.packet.outgoing.node;

import net.taskwolf.core.packet.PacketBuffer;
import net.taskwolf.core.worker.packet.outgoing.PacketOutgoing;

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
