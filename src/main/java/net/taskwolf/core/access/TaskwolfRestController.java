package net.taskwolf.core.access;

import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.user.User;
import net.taskwolf.core.user.UserDatabaseTable;

import java.security.Key;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Getter(AccessLevel.PROTECTED)
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskwolfRestController {
  private final Key secretKey;
  private final UserDatabaseTable userDatabaseTable;

  /**
   * Is used to find the id of the user that send the request
   * @param request The request
   * @return The id of the user
   */
  protected UUID findUserId(HttpServletRequest request) {
    return findUserId(findApiKey(request));
  }

  /**
   * Is used to find the id of a user inside an api key
   * @param apiKey The api key
   * @return The id of the user
   */
  protected UUID findUserId(String apiKey) {
    return UUID.fromString(Jwts.parser().setSigningKey(secretKey).build()
      .parseClaimsJws(apiKey).getPayload().get("id", String.class));
  }

  /**
   * Is used to find the user that send the request
   * @param request The request
   * @return A future that contains the user
   */
  protected CompletableFuture<User> findUser(HttpServletRequest request) {
    var apiKey = request.getHeader("Authorization").replace("Bearer ", "");
    return userDatabaseTable.findUser(findUserId(apiKey));
  }

  /**
   * Is used to find the api key that is sent via a request
   * @param request The request
   * @return The api key
   */
  protected String findApiKey(HttpServletRequest request) {
    return request.getHeader("Authorization").replace("Bearer ", "");
  }

  /**
   * Checks whether an api key is valid
   * @param apiKey The api key
   * @return Is true if api key is valid, otherwise false
   */
  protected boolean isValidApiKey(String apiKey) {
    try {
      Jwts.parser()
        .setSigningKey(secretKey)
        .build()
        .parseClaimsJws(apiKey);
      return true;
    } catch (Exception exception) {
      return false;
    }
  }
}
