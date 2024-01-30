package net.taskwolf.core.distribution.client.packet.node;

import net.taskwolf.core.distribution.client.packet.PacketOutgoing;
import net.taskwolf.core.distribution.packet.PacketBuffer;

public final class PacketOutgoingModuleUnload extends PacketOutgoing {
  private final String module;

  public PacketOutgoingModuleUnload(String module) {
    super(0x06);
    this.module = module;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeString(module);
  }
}