package net.taskwolf.core.distribution.server.packet;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.server.packet.inbound.PacketIncoming;

import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public class PacketRegistry {
  private final List<PacketIncoming> packets = Lists.newArrayList();

  public void registerPacket(Class<? extends PacketIncoming> packet) throws Exception {
    packets.add(packet.getConstructor().newInstance());
  }

  public void unregisterPacket(int id) {
    var packetOptional = packets.stream()
      .filter(entry -> entry.id() == id).findFirst();
    packetOptional.ifPresent(packets::remove);
  }

  public Optional<Class<? extends PacketIncoming>> findPacket(int id) {
    var packetOptional = packets.stream()
      .filter(entry -> entry.id() == id).findFirst();
    return packetOptional.map(PacketIncoming::getClass);
  }
}
