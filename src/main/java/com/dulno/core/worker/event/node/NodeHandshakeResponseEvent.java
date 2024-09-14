package com.dulno.core.worker.event.node;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import com.dulno.core.event.Event;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class NodeHandshakeResponseEvent extends Event {
  private final boolean success;
}
