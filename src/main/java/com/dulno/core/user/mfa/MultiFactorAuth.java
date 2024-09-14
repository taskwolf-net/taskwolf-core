package com.dulno.core.user.mfa;

import com.dulno.core.user.User;
import com.dulno.core.user.UserDatabaseTable;
import com.google.common.collect.Lists;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.recovery.RecoveryCodeGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public final class MultiFactorAuth {
  private final MultiFactorAuthDatabaseTable multiFactorAuthDatabaseTable;
  private final UserDatabaseTable userDatabaseTable;
  private final UUID userId;

  public CompletableFuture<Void> setup() {
    return multiFactorAuthDatabaseTable.insertAuth(userId, generateSecret(),
      generateRecoveryCodes());
  }

  private String generateSecret() {
    var secretGenerator = new DefaultSecretGenerator();
    return secretGenerator.generate();
  }

  private List<String> generateRecoveryCodes() {
    var recoveryCodes = new RecoveryCodeGenerator();
    return Lists.newArrayList(recoveryCodes.generateCodes(16));
  }

  public CompletableFuture<byte[]> generateQRCode() {
    return userDatabaseTable.findUser(userId)
      .thenCompose(user -> multiFactorAuthDatabaseTable.findAuth(userId)
        .thenApplyAsync(auth -> buildQRCode(user, auth.secret())));
  }

  private byte[] buildQRCode(User user, String secret) {
    var data = new QrData.Builder()
      .label(user.email())
      .secret(secret)
      .issuer("Dulno")
      .algorithm(HashingAlgorithm.SHA1)
      .digits(6)
      .period(30)
      .build();
    var generator = new ZxingPngQrGenerator();
    try {
      return generator.generate(data);
    } catch (Exception exception) {
      exception.printStackTrace();
      return new byte[0];
    }
  }

  public CompletableFuture<Boolean> verifyCode(String code) {
    return multiFactorAuthDatabaseTable.authExists(userId)
      .thenCompose(exists -> verifyCode(code, exists));
  }

  private CompletableFuture<Boolean> verifyCode(
    String code, boolean multiFactorAuthEnabled
  ) {
    if (!multiFactorAuthEnabled) {
      return CompletableFuture.completedFuture(true);
    }
    if (code.contains("-")) {
      return verifyRecoveryCode(code);
    }
    var timeProvider = new SystemTimeProvider();
    var codeGenerator = new DefaultCodeGenerator();
    var verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
    return multiFactorAuthDatabaseTable.findAuth(userId)
      .thenApplyAsync(auth -> verifier.isValidCode(auth.secret(), code));
  }

  public CompletableFuture<Boolean> verifyRecoveryCode(String recoveryCode) {
    return multiFactorAuthDatabaseTable.findAuth(userId)
      .thenApply(auth -> auth.recoveryCodes().contains(recoveryCode));
  }
}
