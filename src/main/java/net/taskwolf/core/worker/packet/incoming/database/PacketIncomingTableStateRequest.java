package net.taskwolf.core.worker.packet.incoming.database;

import net.taskwolf.core.packet.PacketBuffer;
import net.taskwolf.core.worker.packet.incoming.PacketIncoming;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.database.transformation.DatabaseTransformationState;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingTableStateRequest extends PacketIncoming {
  private String tableClass;
  private DatabaseTransformationState state;

  public PacketIncomingTableStateRequest() {
    super(0x37);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    tableClass = buffer.readString();
    state = DatabaseTransformationState.valueOf(buffer.readString());
  }
}
