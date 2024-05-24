package net.taskwolf.core.workflow.component.input;

import net.taskwolf.core.user.User;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface InputComponentSelect {
  /**
   * Is used to determine the selection options of component select inputs
   * @param user The user that sent the request
   * @param target The id of the user / organization (target)
   * @param previousInputs The previous selected select inputs (sequential)
   * @return The future list of select options (each string must be in json
   * format and must have a key "identifier" and a key "name")
   */
  CompletableFuture<List<String>> compile(User user, UUID target, Map<String, String> previousInputs);
}
