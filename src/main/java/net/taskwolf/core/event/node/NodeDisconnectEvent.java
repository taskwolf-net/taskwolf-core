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
  public enum DisconnectReason {
    CONNECTION_FAILED,
    SHUTDOWN,
    TIME_OUT;

    public boolean isConnectionFailed() {
      return this == DisconnectReason.CONNECTION_FAILED;
    }

    public boolean isShutdown() {
      return this == DisconnectReason.SHUTDOWN;
    }

    public boolean isTimeOut() {
      return this == DisconnectReason.TIME_OUT;
    }
  }

  private final DistributionClient client;
  private final DisconnectReason reason;
}
