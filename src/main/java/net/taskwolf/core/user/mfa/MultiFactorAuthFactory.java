package net.taskwolf.core.user.mfa;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.user.UserDatabaseTable;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class MultiFactorAuthFactory {
  private final MultiFactorAuthDatabaseTable multiFactorAuthDatabaseTable;
  private final UserDatabaseTable userDatabaseTable;

  public MultiFactorAuth createAuth(UUID userId) {
    return MultiFactorAuth.create(multiFactorAuthDatabaseTable, userDatabaseTable,
      userId);
  }
}
