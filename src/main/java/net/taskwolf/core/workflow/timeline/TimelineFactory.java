package net.taskwolf.core.workflow.timeline;

import lombok.RequiredArgsConstructor;
import net.taskwolf.core.iterator.AsyncIterator;
import net.taskwolf.core.workflow.timeline.entry.TimelineEntryFactory;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public final class TimelineFactory {
  private final TimelineDatabaseTable timelineDatabaseTable;
  private final TimelineEntryFactory timelineEntryFactory;

  public CompletableFuture<Timeline> createTimeline(UUID workflowId) {
    var futureResponse = new CompletableFuture<Timeline>();
    timelineDatabaseTable.findEntriesByWorkflow(workflowId).thenApply(databaseEntries ->
      AsyncIterator.execute(databaseEntries, entry ->
          timelineEntryFactory.create(entry.time(), entry.type(), entry.content()),
        databaseEntries.size(), entries -> futureResponse.complete(Timeline.create(entries))));
    return futureResponse;
  }
}
