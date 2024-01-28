package net.taskwolf.core.distribution.server.packet.node;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.packet.PacketBuffer;
import net.taskwolf.core.distribution.server.packet.PacketIncoming;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingHandshakeResponse extends PacketIncoming {
  private boolean success;

  public PacketIncomingHandshakeResponse() {
    super(0x01);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    success = buffer.raw().readBoolean();
  }
}
