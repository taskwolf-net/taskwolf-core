package net.taskwolf.core.command.implementation;

import net.taskwolf.core.CoreModule;
import net.taskwolf.core.command.Command;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.module.ModuleLoader;

import java.io.File;

public final class ModuleCommand extends Command {
  public static ModuleCommand create(
    Log log, ModuleLoader moduleLoader, CoreModule coreModule
  ) {
    return new ModuleCommand(log, moduleLoader, coreModule);
  }

  private final ModuleLoader moduleLoader;
  private final CoreModule coreModule;

  private ModuleCommand(Log log, ModuleLoader moduleLoader, CoreModule coreModule) {
    super(log, "module", new String[0], new String[] {"load <file>",
      "unload <name>", "reload <name>"});
    this.moduleLoader = moduleLoader;
    this.coreModule = coreModule;
  }

  @Override
  public boolean execute(String[] arguments) throws Exception {
    if (arguments.length == 0) {
      return false;
    }
    if (arguments[0].equalsIgnoreCase("load")) {
      return loadModule(arguments);
    }
    if (arguments[0].equalsIgnoreCase("unload")) {
      return unloadModule(arguments);
    }
    if (arguments[0].equalsIgnoreCase("reload")) {
      return reloadModule(arguments);
    }
    return false;
  }

  private boolean loadModule(String[] arguments) throws Exception {
    if (arguments.length != 2) {
      return false;
    }
    var file = new File(arguments[1]);
    if (!file.exists()) {
      log().info("File '" + file + "' does not exist");
      return true;
    }
    if (!moduleLoader.loadModule(file, coreModule)) {
      log().info("Module is already loaded");
    }
    return true;
  }

  private boolean unloadModule(String[] arguments) throws Exception {
    if (arguments.length != 2) {
      return false;
    }
    var name = arguments[1];
    if (!moduleLoader.unloadModule(name)) {
      log().info("Could not find module with name " + name);
    }
    return true;
  }

  private boolean reloadModule(String[] arguments) throws Exception {
    if (arguments.length != 2) {
      return false;
    }
    var name = arguments[1];
    if (!moduleLoader.reloadModule(name, coreModule)) {
      log().info("Could not find module with name " + name);
    }
    return true;
  }
}
