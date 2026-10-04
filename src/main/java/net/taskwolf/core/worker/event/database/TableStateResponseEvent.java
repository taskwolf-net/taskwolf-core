package net.taskwolf.core.worker.event.database;

import net.taskwolf.core.database.transformation.DatabaseTransformationState;
import net.taskwolf.core.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class TableStateResponseEvent extends Event {
  private final String tableClass;
  private final DatabaseTransformationState state;
}