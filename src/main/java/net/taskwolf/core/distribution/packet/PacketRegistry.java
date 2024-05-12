package net.taskwolf.core.distribution.packet;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.server.packet.PacketIncoming;

import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public class PacketRegistry {
  private final List<PacketIncoming> packets = Lists.newArrayList();

  /**
   * Registers a new incoming packet
   * @param packet The incoming packet that is to be registered
   * @throws Exception
   */
  public void registerPacket(Class<? extends PacketIncoming> packet) throws Exception {
    packets.add(packet.getConstructor().newInstance());
  }

  /**
   * Unregisters a incoming packet
   * @param id The id of the incoming packet that is to be unregistered
   */
  public void unregisterPacket(int id) {
    var packetOptional = packets.stream()
      .filter(entry -> entry.id() == id).findFirst();
    packetOptional.ifPresent(packets::remove);
  }

  /**
   * Is used to find a registered incoming packet by its id
   * @param id The id of the packet
   * @return The incoming packet if it could be found
   */
  public Optional<Class<? extends PacketIncoming>> findPacket(int id) {
    var packetOptional = packets.stream()
      .filter(entry -> entry.id() == id).findFirst();
    return packetOptional.map(PacketIncoming::getClass);
  }
}
