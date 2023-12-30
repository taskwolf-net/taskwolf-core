package net.taskwolf.core.account;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AccountLink {
  CompletableFuture<Boolean> accountExists(UUID id);

  CompletableFuture<List<String>> findAccounts(UUID id);

  void removeAccount(UUID id, String identifier);

  String registrationUrl(UUID id, String apiKey);

  String description();
}
