package net.taskwolf.core.distribution;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public final class DistributionNodeCondition {
  private final List<String> modules = Lists.newArrayList();

  /**
   * Registers a new module
   * @param module The new module that is about to get registered
   */
  public void addModule(String module) {
    modules.add(module);
  }

  /**
   * Registers multiple modules
   * @param targetModules The list of new modules that are about to get registered
   */
  public void addMultipleModules(List<String> targetModules) {
    modules.addAll(targetModules);
  }

  /**
   * Unregisters a module
   * @param module The module that will be unregistered
   */
  public void removeModule(String module) {
    modules.remove(module);
  }

  /**
   * Unregisters multiple modules
   * @param targetModules The list of modules that will be unregistered
   */
  public void removeMultipleModules(List<String> targetModules) {
    modules.removeAll(targetModules);
  }

  /**
   * Checks whether a module is loaded by node
   * @param module The module that will be checked
   * @return Is true when the module is loaded by the node, otherwise false
   */
  public boolean isModuleLoaded(String module) {
    return modules.contains(module);
  }

  /**
   * Is used to find all loaded modules of node
   * @return The list of loaded modules
   */
  public List<String> findLoadedModules() {
    return List.copyOf(modules);
  }
}
