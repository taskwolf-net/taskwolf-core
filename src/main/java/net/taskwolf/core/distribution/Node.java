package net.taskwolf.core.distribution;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class Node {
  private final String hostname;
  private final int redisPort;
  private final int distributionPort;
  private final int restPort;
  private final NodeType nodeType;
}
