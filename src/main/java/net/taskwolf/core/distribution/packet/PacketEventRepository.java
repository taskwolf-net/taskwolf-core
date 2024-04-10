package net.taskwolf.core.distribution.packet;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.client.DistributionClient;
import net.taskwolf.core.distribution.server.packet.PacketIncoming;
import net.taskwolf.core.event.Event;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public class PacketEventRepository {
  private final Map<Class<? extends PacketIncoming>, PacketEvent> events =
    Maps.newHashMap();

  public <P extends PacketIncoming> void registerEvent(
    Class<P> eventClass, BiFunction<DistributionClient, P, Event> eventFunction
  ) {
    events.put(eventClass, (client, packet) ->
      eventFunction.apply(client, (P) packet));
  }

  public <P extends PacketIncoming> void unregisterEvent(Class<P> eventClass) {
    events.remove(eventClass);
  }

  public <P extends PacketIncoming> Optional<PacketEvent> findEvent(
    Class<P> eventClass
  ) {
    return Optional.ofNullable(events.get(eventClass));
  }
}
