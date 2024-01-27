package net.taskwolf.core.module;

import com.google.common.collect.Lists;
import com.google.inject.Injector;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.account.AccountLink;
import net.taskwolf.core.action.ActionFactory;
import net.taskwolf.core.action.ActionInformation;
import net.taskwolf.core.command.Command;
import net.taskwolf.core.command.CommandRegistry;
import net.taskwolf.core.event.Hook;
import net.taskwolf.core.event.HookRegistry;
import net.taskwolf.core.trigger.TriggerFactory;
import net.taskwolf.core.trigger.TriggerInformation;

import java.util.List;

@Getter(AccessLevel.PROTECTED)
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Module {
  private final Injector injector;

  public abstract void enable() throws Exception;

  public abstract void disable() throws Exception;

  public TriggerFactory triggerFactory() {
    return null;
  }

  public ActionFactory actionFactory() {
    return null;
  }

  public AccountLink accountLink() {
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
    injector.getInstance(CommandRegistry.class).register(command);
  }

  public void unregisterCommand(Command command) {
    injector.getInstance(CommandRegistry.class).unregister(command);
  }

  public void registerHook(Hook hook) {
    injector.getInstance(HookRegistry.class).register(hook);
  }

  public void unregisterHook(Hook hook) {
    injector.getInstance(HookRegistry.class).unregister(hook);
  }
}
