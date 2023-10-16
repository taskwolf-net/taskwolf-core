package net.taskwolf.core.action;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class ActionInformation {
  private final String name;
  private final String description;
  //TODO: EXTEND INFORMATION
}

