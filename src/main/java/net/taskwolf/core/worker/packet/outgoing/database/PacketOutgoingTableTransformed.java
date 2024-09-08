package net.taskwolf.core.worker.packet.outgoing.database;

import net.taskwolf.core.packet.PacketBuffer;
import net.taskwolf.core.worker.packet.outgoing.PacketOutgoing;

public final class PacketOutgoingTableTransformed extends PacketOutgoing {
  private final String tableClass;

  public PacketOutgoingTableTransformed(String tableClass) {
    super(0x36);
    this.tableClass = tableClass;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeString(tableClass);
  }
}
