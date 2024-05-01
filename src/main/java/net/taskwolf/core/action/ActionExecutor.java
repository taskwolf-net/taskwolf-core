package net.taskwolf.core.action;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface ActionExecutor {
  CompletableFuture<ActionResult> execute(Map<String, Object> information);
}
