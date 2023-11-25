package net.taskwolf.core.account;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AccountLink {
  CompletableFuture<Boolean> accountExists(UUID userId);

  CompletableFuture<List<String>> findAccounts(UUID userId);

  void removeAccount(UUID userId, String identifier);

  String registrationUrl(String apiKey);

  String description();
}
