package net.taskwolf.core.worker.packet.incoming.database;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.database.transformation.DatabaseTransformationState;
import net.taskwolf.core.packet.PacketBuffer;
import net.taskwolf.core.worker.packet.incoming.PacketIncoming;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingTableStateResponse extends PacketIncoming {
  private String tableClass;
  private DatabaseTransformationState state;

  public PacketIncomingTableStateResponse() {
    super(0x39);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    tableClass = buffer.readString();
    state = DatabaseTransformationState.valueOf(buffer.readString());
  }
}
