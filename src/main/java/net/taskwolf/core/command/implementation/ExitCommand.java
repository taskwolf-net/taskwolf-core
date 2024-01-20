package net.taskwolf.core.command.implementation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.taskwolf.core.command.Command;
import net.taskwolf.core.log.Log;

@Singleton
public final class ExitCommand extends Command {
  public static ExitCommand create(Log log) {
    return new ExitCommand(log);
  }

  @Inject
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
