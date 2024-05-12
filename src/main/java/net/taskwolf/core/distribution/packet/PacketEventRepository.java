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

/**
 * Is used to streamline the process of event calling when a new packet is received
 */
@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public class PacketEventRepository {
  private final Map<Class<? extends PacketIncoming>, PacketEvent> events =
    Maps.newHashMap();

  /**
   * Registers a new packet event
   * @param eventClass The class of the incoming packet
   * @param eventFunction The function that creates the event
   * @param <P> The type of the incoming packet
   */
  public <P extends PacketIncoming> void registerEvent(
    Class<P> eventClass, BiFunction<DistributionClient, P, Event> eventFunction
  ) {
    events.put(eventClass, (client, packet) ->
      eventFunction.apply(client, (P) packet));
  }

  /**
   * Unregisters a packet event
   * @param eventClass The class of the incoming packet
   * @param <P> The type of the incoming packet
   */
  public <P extends PacketIncoming> void unregisterEvent(Class<P> eventClass) {
    events.remove(eventClass);
  }

  /**
   * Is used to find a packet event by incoming packet class
   * @param eventClass The incoming packet class
   * @return The packet event if it could be found
   * @param <P> The type of the incoming packet
   */
  public <P extends PacketIncoming> Optional<PacketEvent> findEvent(
    Class<P> eventClass
  ) {
    return Optional.ofNullable(events.get(eventClass));
  }
}
