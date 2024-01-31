package net.taskwolf.core.distribution.server.packet.node;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.packet.PacketBuffer;
import net.taskwolf.core.distribution.server.packet.PacketIncoming;

import java.util.List;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingHandshakeResponse extends PacketIncoming {
  private boolean success;
  private UUID nodeId;
  private List<String> loadedModules;

  public PacketIncomingHandshakeResponse() {
    super(0x01);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    success = buffer.raw().readBoolean();
    if (!success) {
      return;
    }
    nodeId = buffer.readUUID();
    var loadedModulesSize = buffer.readVarInt();
    loadedModules = Lists.newArrayList();
    for (var i = 0; i < loadedModulesSize; i++) {
      loadedModules.add(buffer.readString());
    }
  }
}
