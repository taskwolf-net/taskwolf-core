package com.dulno.core.worker.packet.outgoing.node;

import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.outgoing.PacketOutgoing;

import java.util.List;

public final class PacketOutgoingModulesLoad extends PacketOutgoing {
  private final List<String> modules;

  public PacketOutgoingModulesLoad(List<String> modules) {
    super(0x05);
    this.modules = modules;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeVarInt(modules.size());
    for (var module : modules) {
      buffer.writeString(module);
    }
  }
}
