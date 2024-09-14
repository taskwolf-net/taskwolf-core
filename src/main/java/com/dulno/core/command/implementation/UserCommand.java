package com.dulno.core.command.implementation;

import com.dulno.core.command.Command;
import com.dulno.core.log.Log;
import com.dulno.core.user.User;
import com.dulno.core.user.UserDatabaseTable;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public final class UserCommand extends Command {
  private final UserDatabaseTable userDatabaseTable;

  @Inject
  private UserCommand(Log log, UserDatabaseTable userDatabaseTable) {
    super(log, "user", new String[0], new String[] {"find <email>"});
    this.userDatabaseTable = userDatabaseTable;
  }

  @Override
  public boolean execute(String[] arguments) {
    if (arguments.length == 0) {
      return false;
    }
    if (arguments[0].equalsIgnoreCase("find")) {
      return findUser(arguments);
    }
    return false;
  }

  private boolean findUser(String[] arguments) {
    if (arguments.length != 2) {
      return false;
    }
    var email = arguments[1];
    userDatabaseTable.userExists(email).thenAccept(exists -> findUser(email, exists));
    return true;
  }

  private void findUser(String email, boolean exists) {
    if (!exists) {
      log().info("Error: Can't find any user with this email!");
      return;
    }
    userDatabaseTable.findUser(email).thenAccept(this::printUser);
  }

  private void printUser(User user) {
    log().info("User:");
    log().info(" - Id: " + user.id());
    log().info(" - Name: " + user.name());
    log().info(" - Email: " + user.email());
    log().info(" - Language: " + user.language());
  }
}
