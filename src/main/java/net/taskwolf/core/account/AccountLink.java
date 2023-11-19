package net.taskwolf.core.account;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AccountLink {
  CompletableFuture<Boolean> accountExists(UUID userId);

  String registrationUrl(String apiKey);

  String description();
}
