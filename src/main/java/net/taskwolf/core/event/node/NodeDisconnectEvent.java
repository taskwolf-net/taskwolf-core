package net.taskwolf.core.event.node;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.client.DistributionClient;
import net.taskwolf.core.event.Event;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class NodeDisconnectEvent extends Event {
  public enum DisconnectType {
    NATURALLY,
    TIME_OUT;

    public boolean isNaturally() {
      return this == DisconnectType.NATURALLY;
    }

    public boolean isTimeOut() {
      return this == DisconnectType.TIME_OUT;
    }
  }

  private final DistributionClient client;
  private final DisconnectType type;
}
