package net.taskwolf.core.command.implementation;

import net.taskwolf.core.command.Command;
import net.taskwolf.core.log.Log;

public final class HelpCommand extends Command {
  public static HelpCommand create(Log log) {
    return new HelpCommand(log);
  }

  private HelpCommand(Log log) {
    super(log, "help", new String[] {"info", "commands"}, new String[0]);
  }

  @Override
  public boolean execute(String[] arguments) {
    log().info("Commands: ");
    log().info("- modules");
    log().info("- distribution");
    log().info("- exit");
    return true;
  }
}
