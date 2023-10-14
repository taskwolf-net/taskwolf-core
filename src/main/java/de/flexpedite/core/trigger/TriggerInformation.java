package de.flexpedite.core.trigger;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class TriggerInformation {
  private final String name;
  private final String description;
  //TODO: EXTEND INFORMATION
}
