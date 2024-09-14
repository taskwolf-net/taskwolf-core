package com.dulno.core.worker.packet.outgoing.database;

import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.outgoing.PacketOutgoing;

public final class PacketOutgoingTableDiscrepancy extends PacketOutgoing {
  private final String tableClass;

  public PacketOutgoingTableDiscrepancy(String tableClass) {
    super(0x34);
    this.tableClass = tableClass;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeString(tableClass);
  }
}
