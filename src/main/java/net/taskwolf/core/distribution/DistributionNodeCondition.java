package net.taskwolf.core.distribution;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public final class DistributionNodeCondition {
  private final List<String> modules = Lists.newArrayList();

  public void addModule(String module) {
    modules.add(module);
  }

  public void removeModule(String module) {
    modules.remove(module);
  }

  public boolean isModuleLoaded(String module) {
    return modules.contains(module);
  }

  public List<String> findLoadedModules() {
    return List.copyOf(modules);
  }
}
