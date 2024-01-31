package net.taskwolf.core.distribution.client.packet.node;

import net.taskwolf.core.distribution.client.packet.PacketOutgoing;
import net.taskwolf.core.distribution.packet.PacketBuffer;

import java.util.UUID;

public final class PacketOutgoingHandshakeRequest extends PacketOutgoing {
  private final String hostname;
  private final int port;
  private final String key;
  private final UUID nodeId;

  public PacketOutgoingHandshakeRequest(
    String hostname, int port, String key, UUID nodeId
  ) {
    super(0x00);
    this.hostname = hostname;
    this.port = port;
    this.key = key;
    this.nodeId = nodeId;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeString(hostname);
    buffer.writeVarInt(port);
    buffer.writeString(key);
    buffer.writeUUID(nodeId);
  }
}
