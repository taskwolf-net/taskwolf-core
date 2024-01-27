package net.taskwolf.core.distribution.server.packet.inbound;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.packet.PacketBuffer;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingHandshake extends PacketIncoming {
  private String hostname;
  private String key;

  public PacketIncomingHandshake() {
    super(0x00);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    hostname = buffer.readString();
    key = buffer.readString();
  }
}
