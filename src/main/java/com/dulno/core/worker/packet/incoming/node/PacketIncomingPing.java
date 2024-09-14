package com.dulno.core.worker.packet.incoming.node;

import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.incoming.PacketIncoming;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingPing extends PacketIncoming {
  private int value;

  public PacketIncomingPing() {
    super(0x02);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    value = buffer.readVarInt();
  }
}
