package net.taskwolf.core.distribution.server.packet.node;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.packet.PacketBuffer;
import net.taskwolf.core.distribution.server.packet.PacketIncoming;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingHandshakeRequest extends PacketIncoming {
  private String hostname;
  private int port;
  private String key;

  public PacketIncomingHandshakeRequest() {
    super(0x00);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    hostname = buffer.readString();
    port = buffer.readVarInt();
    key = buffer.readString();
  }
}
