package net.taskwolf.core.distribution.server.packet.node;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.packet.PacketBuffer;
import net.taskwolf.core.distribution.server.packet.PacketIncoming;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingModuleLoad extends PacketIncoming {
  private String module;

  public PacketIncomingModuleLoad() {
    super(0x05);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    module = buffer.readString();
  }
}

