package net.taskwolf.core.distribution.client;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public final class DistributionClientCondition {
  private final List<String> modules = Lists.newArrayList();

  public void addModule(String module) {
    modules.add(module);
  }

  public void removeModule(String module) {
    modules.remove(module);
  }

  public List<String> findLoadedModules() {
    return List.copyOf(modules);
  }
}
