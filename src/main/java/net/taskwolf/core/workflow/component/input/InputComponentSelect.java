package net.taskwolf.core.workflow.component.input;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface InputComponentSelect {
  /**
   * Is used to determine the selection options of component select inputs
   * @param id The id of the user / organization (target)
   * @param previousInputs The previous selected select inputs (sequential)
   * @return The future list of select options (each string must be in json
   * format and must have a key "identifier" and a key "name")
   */
  CompletableFuture<List<String>> compile(UUID id, Map<String, String> previousInputs);
}
