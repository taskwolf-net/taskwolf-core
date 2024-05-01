package net.taskwolf.core.trigger;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Trigger {
  String type();
  TriggerInformation information();
  CompletableFuture<Void> insert(UUID triggerId, Map<String, Object> content);
  CompletableFuture<List<UUID>> findEntries(String condition);
  CompletableFuture<Void> delete(UUID triggerId);
}
