package com.dulno.core.worker.packet.incoming.database;

import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.incoming.PacketIncoming;
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
