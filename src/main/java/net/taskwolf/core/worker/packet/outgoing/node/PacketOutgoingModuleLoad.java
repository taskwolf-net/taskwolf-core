package net.taskwolf.core.worker.packet.outgoing.node;

import net.taskwolf.core.packet.PacketBuffer;
import net.taskwolf.core.worker.packet.outgoing.PacketOutgoing;

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
