package net.taskwolf.core.distribution.server.packet.user;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.packet.PacketBuffer;
import net.taskwolf.core.distribution.server.packet.PacketIncoming;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingUserDelete extends PacketIncoming {
  private UUID user;

  public PacketIncomingUserDelete() {
    super(0x06);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    user = buffer.readUUID();
  }
}
