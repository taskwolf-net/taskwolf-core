package net.taskwolf.core.distribution.client.packet.node;

import net.taskwolf.core.distribution.client.packet.PacketOutgoing;
import net.taskwolf.core.distribution.packet.PacketBuffer;

public final class PacketOutgoingHandshakeRequest extends PacketOutgoing {
  private final String hostname;
  private final int port;
  private final String key;

  public PacketOutgoingHandshakeRequest(String hostname, int port, String key) {
    super(0x00);
    this.hostname = hostname;
    this.port = port;
    this.key = key;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeString(hostname);
    buffer.writeVarInt(port);
    buffer.writeString(key);
  }
}
