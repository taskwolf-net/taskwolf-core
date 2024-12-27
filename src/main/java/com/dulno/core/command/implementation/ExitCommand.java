package com.dulno.core.command.implementation;

import com.dulno.core.command.Command;
import com.dulno.core.log.Log;
import com.dulno.core.worker.WorkerDistribution;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.dulno.core.module.ModuleLoader;

@Singleton
public final class ExitCommand extends Command {
  private final WorkerDistribution distribution;
  private final ModuleLoader moduleLoader;

  @Inject
  private ExitCommand(
          Log log, WorkerDistribution distribution, ModuleLoader moduleLoader
  ) {
    super(log, "exit", new String[] {"shutdown"}, new String[0]);
    this.distribution = distribution;
    this.moduleLoader = moduleLoader;
  }

  @Override
  public boolean execute(String[] arguments) throws Exception {
    log().info("Ending Dulno - Core");
    distribution.disconnect();
    for (var module : moduleLoader.allRegisteredModules()) {
      module.module().preDisable();
      module.module().disable();
      module.module().postDisable();
    }
    System.exit(0);
    return true;
  }
}
