package net.taskwolf.core.worker.event.database;

import net.taskwolf.core.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class TableTransformEvent extends Event {
  private final String tableClass;
}