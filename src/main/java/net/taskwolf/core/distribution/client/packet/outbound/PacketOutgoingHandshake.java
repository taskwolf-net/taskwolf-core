package net.taskwolf.core.distribution.client.packet.outbound;

import net.taskwolf.core.distribution.packet.PacketBuffer;

public final class PacketOutgoingHandshake extends PacketOutgoing {
  private final String hostname;
  private final String key;

  public PacketOutgoingHandshake(String hostname, String key) {
    super(0x00);
    this.hostname = hostname;
    this.key = key;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeString(hostname);
    buffer.writeString(key);
  }
}
