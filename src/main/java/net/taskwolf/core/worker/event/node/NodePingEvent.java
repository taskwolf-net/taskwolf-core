package net.taskwolf.core.worker.event.node;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.event.Event;
import net.taskwolf.core.worker.client.WorkerProxyClient;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class NodePingEvent extends Event {
  private final WorkerProxyClient client;
  private final int value;
}