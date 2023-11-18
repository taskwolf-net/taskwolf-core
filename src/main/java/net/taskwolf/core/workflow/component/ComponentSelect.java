package net.taskwolf.core.workflow.component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ComponentSelect {
  CompletableFuture<List<String>> compile(UUID user, Map<String, String> previousInputs);
}
