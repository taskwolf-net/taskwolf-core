package net.taskwolf.core.user.mfa;

import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public final class MultiFactorAuth {
  private final MultiFactorAuthDatabaseTable multiFactorAuthDatabaseTable;
  private final UUID userId;

  public CompletableFuture<Void> setup() {
    return null;
  }

  public CompletableFuture<byte[]> generateQRCode() {
    return null;
  }

  public CompletableFuture<String> findSecret() {
    return null;
  }

  public boolean verifyCode(String code) {
    return true;
  }
}
