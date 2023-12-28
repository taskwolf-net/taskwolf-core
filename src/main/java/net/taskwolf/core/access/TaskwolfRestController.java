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

  protected UUID findUserId(HttpServletRequest request) {
    return findUserId(findApiKey(request));
  }

  protected UUID findUserId(String apiKey) {
    return UUID.fromString(Jwts.parser().setSigningKey(secretKey).build()
      .parseClaimsJws(apiKey).getPayload().get("id", String.class));
  }

  protected CompletableFuture<User> findUser(HttpServletRequest request) {
    var apiKey = request.getHeader("Authorization").replace("Bearer ", "");
    var email = Jwts.parser().setSigningKey(secretKey).build()
      .parseClaimsJws(apiKey).getPayload().get("email", String.class);
    return userDatabaseTable.findUser(email);
  }

  protected String findApiKey(HttpServletRequest request) {
    return request.getHeader("Authorization").replace("Bearer ", "");
  }

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
