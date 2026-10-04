package net.taskwolf.core.worker.packet.incoming.database;

import net.taskwolf.core.packet.PacketBuffer;
import net.taskwolf.core.worker.packet.incoming.PacketIncoming;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingTableTransform extends PacketIncoming {
  private String tableClass;

  public PacketIncomingTableTransform() {
    super(0x35);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    tableClass = buffer.readString();
  }
}
