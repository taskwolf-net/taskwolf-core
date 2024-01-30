package net.taskwolf.core.distribution.client.packet.user;

import net.taskwolf.core.distribution.client.packet.PacketOutgoing;
import net.taskwolf.core.distribution.packet.PacketBuffer;

import java.util.UUID;

public final class PacketOutgoingUserDelete extends PacketOutgoing {;
  private final UUID user;

  public PacketOutgoingUserDelete(UUID user) {
    super(0x08);
    this.user = user;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeUUID(user);
  }
}
