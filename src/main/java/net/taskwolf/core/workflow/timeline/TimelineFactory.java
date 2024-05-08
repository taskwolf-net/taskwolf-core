package net.taskwolf.core.workflow.timeline;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.iterator.AsyncIterator;
import net.taskwolf.core.workflow.timeline.entry.TimelineEntryFactory;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class TimelineFactory {
  private final TimelineDatabaseTable timelineDatabaseTable;
  private final TimelineEntryFactory timelineEntryFactory;

  public CompletableFuture<Timeline> createTimeline(UUID workflowId) {
    var futureResponse = new CompletableFuture<Timeline>();
    timelineDatabaseTable.findEntriesByWorkflow(workflowId).thenApply(databaseEntries ->
      AsyncIterator.execute(databaseEntries, entry ->
          timelineEntryFactory.create(entry.time(), entry.type(), entry.content()))
        .thenAccept(entries -> futureResponse.complete(Timeline.create(entries))));
    return futureResponse;
  }
}
