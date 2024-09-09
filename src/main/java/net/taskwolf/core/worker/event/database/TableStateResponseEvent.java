package net.taskwolf.core.worker.event.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.database.transformation.DatabaseTransformationState;
import net.taskwolf.core.event.Event;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class TableStateResponseEvent extends Event {
  private final String tableClass;
  private final DatabaseTransformationState state;
}