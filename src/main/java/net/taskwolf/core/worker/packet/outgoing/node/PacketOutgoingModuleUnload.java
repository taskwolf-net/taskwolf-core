package net.taskwolf.core.worker.packet.outgoing.node;

import net.taskwolf.core.packet.PacketBuffer;
import net.taskwolf.core.worker.packet.outgoing.PacketOutgoing;

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