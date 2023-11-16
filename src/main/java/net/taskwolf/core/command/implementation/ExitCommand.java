package net.taskwolf.core.command.implementation;

import net.taskwolf.core.command.Command;
import net.taskwolf.core.log.Log;

public final class ExitCommand extends Command {
  public static ExitCommand create(Log log) {
    return new ExitCommand(log);
  }

  private ExitCommand(Log log) {
    super(log, "exit", new String[] {"shutdown"}, new String[0]);
  }

  @Override
  public boolean execute(String[] arguments) {
    log().info("Ending Taskwolf - Core");
    System.exit(0);
    return true;
  }
}
