package com.dulno.core.worker.packet.incoming.node;

import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.incoming.PacketIncoming;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingHandshakeResponse extends PacketIncoming {
  private boolean success;

  public PacketIncomingHandshakeResponse() {
    super(0x01);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    success = buffer.raw().readBoolean();
  }
}
