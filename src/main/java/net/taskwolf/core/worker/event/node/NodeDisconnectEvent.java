package net.taskwolf.core.worker.event.node;

import net.taskwolf.core.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class NodeDisconnectEvent extends Event {
  public enum DisconnectReason {
    CONNECTION_FAILED,
    TIME_OUT;

    public boolean isConnectionFailed() {
      return this == DisconnectReason.CONNECTION_FAILED;
    }

    public boolean isTimeOut() {
      return this == DisconnectReason.TIME_OUT;
    }
  }

  private final DisconnectReason reason;
}
