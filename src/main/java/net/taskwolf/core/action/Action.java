package net.taskwolf.core.action;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Action<T extends ActionExecutor> {
  String type();
  ActionInformation information();
  void initialize();
  CompletableFuture<Void> insert(UUID actionId, Map<String, Object> content);
  CompletableFuture<Map<String, Object>> findContent(UUID actionId);
  CompletableFuture<T> build(UUID actionId);
  CompletableFuture<Void> delete(UUID actionId);
}
