package com.dulno.core.worker.event.node;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import com.dulno.core.event.Event;
import com.dulno.core.worker.client.WorkerProxyClient;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class NodePingEvent extends Event {
  private final WorkerProxyClient client;
  private final int value;
}