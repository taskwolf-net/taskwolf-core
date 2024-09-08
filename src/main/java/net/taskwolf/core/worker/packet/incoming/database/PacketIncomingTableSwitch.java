package net.taskwolf.core.worker.packet.incoming.database;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.packet.PacketBuffer;
import net.taskwolf.core.worker.packet.incoming.PacketIncoming;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingTableSwitch extends PacketIncoming {
  private String tableClass;

  public PacketIncomingTableSwitch() {
    super(0x37);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    tableClass = buffer.readString();
  }
}
