package net.taskwolf.core.distribution.packet;

import net.taskwolf.core.distribution.client.DistributionClient;
import net.taskwolf.core.distribution.server.packet.PacketIncoming;
import net.taskwolf.core.event.Event;

public interface PacketEvent {
  Event process(DistributionClient client, PacketIncoming packet);
}
