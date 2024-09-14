package com.dulno.core.access;

import com.dulno.core.user.User;
import com.dulno.core.user.UserDatabaseTable;
import jakarta.servlet.http.HttpServletRequest;

import java.security.Key;
import java.util.concurrent.CompletableFuture;

public class DulnoHomeRestController extends DulnoRestController {
  protected DulnoHomeRestController(
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
