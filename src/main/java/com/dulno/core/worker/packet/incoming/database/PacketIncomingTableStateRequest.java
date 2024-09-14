package com.dulno.core.worker.packet.incoming.database;

import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.incoming.PacketIncoming;
import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.database.transformation.DatabaseTransformationState;

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
