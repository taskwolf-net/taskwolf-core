package net.taskwolf.core.distribution;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class Node {
  private final NodeType type;
  private final String hostname;
  private final int restPort;
  private final int distributionPort;
  private final String distributionKey;

  public String information() {
    return hostname + " [" + type.toString() + "]";
  }
}
