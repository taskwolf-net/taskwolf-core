package net.taskwolf.core.distribution.packet;

import net.taskwolf.core.distribution.client.DistributionClient;
import net.taskwolf.core.distribution.server.packet.PacketIncoming;
import net.taskwolf.core.event.Event;

public interface PacketEvent {
  /**
   * Creates a new event when packet is received
   * @param client The client that send the packet
   * @param packet The packet that has been received
   * @return The created event
   */
  Event process(DistributionClient client, PacketIncoming packet);
}
