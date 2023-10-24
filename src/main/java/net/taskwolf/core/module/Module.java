package net.taskwolf.core.module;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.CoreModule;
import net.taskwolf.core.action.ActionFactory;
import net.taskwolf.core.action.ActionInformation;
import net.taskwolf.core.command.Command;
import net.taskwolf.core.trigger.TriggerFactory;
import net.taskwolf.core.trigger.TriggerInformation;

import java.util.List;

@Getter(AccessLevel.PROTECTED)
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Module {
  private final CoreModule coreModule;

  public abstract void enable() throws Exception;

  public abstract void disable() throws Exception;

  public TriggerFactory triggerFactory() {
    return null;
  }

  public ActionFactory actionFactory() {
    return null;
  }

  public abstract ModuleInformation moduleInformation();

  public List<TriggerInformation> triggerInformation() {
    return Lists.newArrayList();
  }

  public List<ActionInformation> actionInformation() {
    return Lists.newArrayList();
  }

  public void registerCommand(Command command) {
    coreModule.commandRegistry().register(command);
  }

  public void unregisterCommand(Command command) {
    coreModule.commandRegistry().unregister(command);
  }
}
