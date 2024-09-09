package net.taskwolf.core.worker.packet.outgoing.database;

import net.taskwolf.core.database.transformation.DatabaseTransformationState;
import net.taskwolf.core.packet.PacketBuffer;
import net.taskwolf.core.worker.packet.outgoing.PacketOutgoing;

public final class PacketOutgoingTableStateRequest extends PacketOutgoing {
  private final String tableClass;
  private final DatabaseTransformationState state;

  public PacketOutgoingTableStateRequest(
    String tableClass, DatabaseTransformationState state
  ) {
    super(0x36);
    this.tableClass = tableClass;
    this.state = state;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeString(tableClass);
    buffer.writeString(state.toString());
  }
}
