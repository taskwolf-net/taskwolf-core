package net.taskwolf.core.locale;

import net.taskwolf.core.user.User;
import net.taskwolf.core.user.UserDatabaseTable;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Singleton
public final class Translation {
  private final UserDatabaseTable userDatabaseTable;
  private final Locale englishLocale;
  private final Locale germanLocale;

  @Inject
  private Translation(
    UserDatabaseTable userDatabaseTable,
    @Named("englishLocale") Locale englishLocale,
    @Named("germanLocale") Locale germanLocale
  ) {
    this.userDatabaseTable = userDatabaseTable;
    this.englishLocale = englishLocale;
    this.germanLocale = germanLocale;
  }

  /**
   * Translates a locale for a user
   * @param userId The id of the user
   * @param key The key of the locale
   * @return A future that contains the translated locale
   */
  public CompletableFuture<String> translate(UUID userId, String key) {
    return userDatabaseTable.findUser(userId).thenApply(user -> translate(user, key));
  }

  /**
   * Translates a locale for a user
   * @param user The user
   * @param key The key of the locale
   * @return A future that contains the translated locale
   */
  public String translate(User user, String key) {
    return translate(user.language(), key);
  }

  /**
   * Translates a locale into a specific language
   * @param language The language
   * @param key The key of the locale
   * @return A future that contains the translated locale
   */
  public String translate(String language, String key) {
    return switch(language.toLowerCase()) {
      case "en" -> englishLocale.findText(key);
      case "de" -> germanLocale.findText(key);
      default -> "LANGUAGE NOT FOUND";
    };
  }
}
