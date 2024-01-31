package net.taskwolf.core.distribution.client.packet.node;

import net.taskwolf.core.distribution.client.packet.PacketOutgoing;
import net.taskwolf.core.distribution.packet.PacketBuffer;

import java.util.List;
import java.util.UUID;

public final class PacketOutgoingHandshakeResponse extends PacketOutgoing {
  private final boolean success;
  private UUID nodeId;
  private List<String> loadedModules;

  public PacketOutgoingHandshakeResponse(
    boolean success, UUID nodeId, List<String> loadedModules
  ) {
    super(0x01);
    this.success = success;
    this.nodeId = nodeId;
    this.loadedModules = loadedModules;
  }

  public PacketOutgoingHandshakeResponse(boolean success) {
    super(0x01);
    this.success = success;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.raw().writeBoolean(success);
    if (!success) {
      return;
    }
    buffer.writeUUID(nodeId);
    buffer.writeVarInt(loadedModules.size());
    for (var module : loadedModules) {
      buffer.writeString(module);
    }
  }
}
