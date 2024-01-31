package net.taskwolf.core.command.implementation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.taskwolf.core.command.Command;
import net.taskwolf.core.distribution.Distribution;
import net.taskwolf.core.log.Log;

@Singleton
public final class ExitCommand extends Command {
  public static ExitCommand create(Log log, Distribution distribution) {
    return new ExitCommand(log, distribution);
  }

  private final Distribution distribution;

  @Inject
  private ExitCommand(Log log, Distribution distribution) {
    super(log, "exit", new String[] {"shutdown"}, new String[0]);
    this.distribution = distribution;
  }

  @Override
  public boolean execute(String[] arguments) {
    log().info("Ending Taskwolf - Core");
    distribution.disconnect();
    System.exit(0);
    return true;
  }
}
