package net.taskwolf.core.event.node;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.Node;
import net.taskwolf.core.event.Event;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class NodeHandshakeRequestEvent extends Event {
  private final Node node;
  private final Channel channel;
  private final String key;
}
