package net.taskwolf.core.command.implementation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.taskwolf.core.command.Command;
import net.taskwolf.core.distribution.Distribution;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.module.ModuleLoader;

@Singleton
public final class ExitCommand extends Command {
  private final Distribution distribution;
  private final ModuleLoader moduleLoader;

  @Inject
  private ExitCommand(
    Log log, Distribution distribution, ModuleLoader moduleLoader
  ) {
    super(log, "exit", new String[] {"shutdown"}, new String[0]);
    this.distribution = distribution;
    this.moduleLoader = moduleLoader;
  }

  @Override
  public boolean execute(String[] arguments) throws Exception {
    log().info("Ending Taskwolf - Core");
    distribution.disconnect();
    for (var module : moduleLoader.allRegisteredModules()) {
      module.module().disable();
    }
    System.exit(0);
    return true;
  }
}
