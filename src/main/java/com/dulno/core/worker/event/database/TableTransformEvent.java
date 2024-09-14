package com.dulno.core.worker.event.database;

import com.dulno.core.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class TableTransformEvent extends Event {
  private final String tableClass;
}