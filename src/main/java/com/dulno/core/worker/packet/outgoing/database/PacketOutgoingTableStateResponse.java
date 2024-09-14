package com.dulno.core.worker.packet.outgoing.database;

import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.outgoing.PacketOutgoing;
import com.dulno.core.database.transformation.DatabaseTransformationState;

public final class PacketOutgoingTableStateResponse extends PacketOutgoing {
  private final String tableClass;
  private final DatabaseTransformationState state;

  public PacketOutgoingTableStateResponse(
    String tableClass, DatabaseTransformationState state
  ) {
    super(0x38);
    this.tableClass = tableClass;
    this.state = state;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeString(tableClass);
    buffer.writeString(state.toString());
  }
}