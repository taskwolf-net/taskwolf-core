package net.taskwolf.core;

import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseRow;
import net.taskwolf.core.database.transformation.DatabaseTransformation;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public final class TestDatabaseTableTransformation implements DatabaseTransformation {
  @Override
  public CompletableFuture<DatabaseRow> transformOldToNew(DatabaseRow oldRow) {
    return CompletableFuture.completedFuture(DatabaseRow.of(oldRow.findCell(0),
      oldRow.findCell(1), "TRANSFORMED"));
  }

  @Override
  public CompletableFuture<DatabaseRow> transformNewToOld(DatabaseRow newRow) {
    return CompletableFuture.completedFuture(DatabaseRow.of(newRow.findCell(0),
      newRow.findCell(1)));
  }
}
