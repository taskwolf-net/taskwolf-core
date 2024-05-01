package net.taskwolf.core.trigger;

import net.taskwolf.core.database.DatabaseRow;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Trigger {
  String type();
  TriggerInformation information();
  CompletableFuture<Void> insert(UUID triggerId, DatabaseRow content);
  CompletableFuture<List<UUID>> findEntries(String condition);
  CompletableFuture<Void> delete(UUID triggerId);
}
