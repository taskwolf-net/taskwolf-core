package net.taskwolf.core.distribution.client.packet.node;

import net.taskwolf.core.distribution.client.packet.PacketOutgoing;
import net.taskwolf.core.distribution.packet.PacketBuffer;

public final class PacketOutgoingModuleLoad extends PacketOutgoing {
  private final String module;

  public PacketOutgoingModuleLoad(String module) {
    super(0x05);
    this.module = module;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeString(module);
  }
}
