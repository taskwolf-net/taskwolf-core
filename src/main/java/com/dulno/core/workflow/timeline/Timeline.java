package com.dulno.core.workflow.timeline;

import lombok.RequiredArgsConstructor;
import com.dulno.core.workflow.timeline.entry.TimelineEntry;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public final class Timeline {
  private final List<TimelineEntry> entries;

  public List<TimelineEntry> findAllEntries() {
    return List.copyOf(entries);
  }
}
