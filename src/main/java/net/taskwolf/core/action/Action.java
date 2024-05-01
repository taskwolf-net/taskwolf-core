package net.taskwolf.core.action;

import net.taskwolf.core.database.DatabaseRow;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Action<T extends ActionExecutor> {
  String type();
  ActionInformation information();
  CompletableFuture<Void> insert(UUID actionId, DatabaseRow content);
  CompletableFuture<T> build(UUID actionId);
  CompletableFuture<Void> delete(UUID actionId);
}
