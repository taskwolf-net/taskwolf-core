package com.dulno.core.worker.event.database;

import com.dulno.core.database.transformation.DatabaseTransformationState;
import com.dulno.core.event.Event;
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