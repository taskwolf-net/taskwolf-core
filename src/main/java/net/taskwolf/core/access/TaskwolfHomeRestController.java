package net.taskwolf.core.access;

import jakarta.servlet.http.HttpServletRequest;
import net.taskwolf.core.user.User;
import net.taskwolf.core.user.UserDatabaseTable;

import java.security.Key;
import java.util.concurrent.CompletableFuture;

public class TaskwolfHomeRestController extends TaskwolfRestController {
  protected TaskwolfHomeRestController(
    Key secretKey, UserDatabaseTable userDatabaseTable
  ) {
    super(secretKey, userDatabaseTable);
  }

  @Override
  protected CompletableFuture<User> findUser(HttpServletRequest request) {
    var apiKey = request.getHeader("Home-Authorization").replace("Bearer ", "");
    return userDatabaseTable().findUser(findUserId(apiKey));
  }

  @Override
  protected String findApiKey(HttpServletRequest request) {
    return request.getHeader("Home-Authorization").replace("Bearer ", "");
  }
}
