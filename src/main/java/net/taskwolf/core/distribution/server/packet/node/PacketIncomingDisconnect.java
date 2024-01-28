package net.taskwolf.core.distribution.server.packet.node;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.packet.PacketBuffer;
import net.taskwolf.core.distribution.server.packet.PacketIncoming;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingDisconnect extends PacketIncoming {
  public PacketIncomingDisconnect() {
    super(0x04);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {

  }
}
