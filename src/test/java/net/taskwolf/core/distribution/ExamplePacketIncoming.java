package net.taskwolf.core.distribution;

import net.taskwolf.core.distribution.packet.PacketBuffer;
import net.taskwolf.core.distribution.server.packet.PacketIncoming;

public final class ExamplePacketIncoming extends PacketIncoming {
  public ExamplePacketIncoming() {
    super(0x00);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {

  }
}