package net.taskwolf.core.event.node;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.client.DistributionClient;
import net.taskwolf.core.event.Event;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
@RequiredArgsConstructor(staticName = "create")
public final class NodeHandshakeResponseEvent extends Event {
  private final DistributionClient client;
  private final boolean success;
  private UUID nodeId;
}
