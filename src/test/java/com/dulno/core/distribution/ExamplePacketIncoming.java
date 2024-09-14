package com.dulno.core.distribution;

import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.incoming.PacketIncoming;

public final class ExamplePacketIncoming extends PacketIncoming {
  public ExamplePacketIncoming() {
    super(0x00);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {

  }
}