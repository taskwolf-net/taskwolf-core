package com.dulno.core.module;

import com.dulno.core.account.AccountLink;
import com.dulno.core.action.ActionRepository;
import com.dulno.core.command.Command;
import com.dulno.core.command.CommandRegistry;
import com.dulno.core.event.Hook;
import com.dulno.core.event.HookRegistry;
import com.dulno.core.trigger.TriggerRepository;
import com.google.inject.Injector;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter(AccessLevel.PROTECTED)
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Module {
  private final Injector injector;

  /**
   * Is called up when the module is to be loaded.
   * Used to initialize the module.
   * @throws Exception
   */
  public abstract void enable() throws Exception;

  /**
   * Is called up when a module is to be unloaded.
   * It is intended to reset the status of the module and release used resources
   * @throws Exception
   */
  public abstract void disable() throws Exception;

  /**
   * Is there to manage the accounts of the module
   * @return The account link of the module
   */
  public AccountLink accountLink() {
    return null;
  }

  /**
   * Specifies basic information that can be displayed externally
   * @return The module information
   */
  public abstract ModuleInformation moduleInformation();

  /**
   * Is used to store the triggers of the module
   * @return The trigger repository
   */
  public TriggerRepository triggerRepository() {
    return TriggerRepository.create();
  }

  /**
   * Is used to store the actions of the module
   * @return The action repository
   */
  public ActionRepository actionRepository() {
    return ActionRepository.create();
  }

  /**
   * Can be called to register a new dulno command line command
   * @param command The command that is to be registered
   */
  public void registerCommand(Command command) {
    injector.getInstance(CommandRegistry.class).register(command);
  }


  /**
   * Is used to unregister a command from command line
   * @param command The command that is to be unregistered
   */
  public void unregisterCommand(Command command) {
    injector.getInstance(CommandRegistry.class).unregister(command);
  }

  /**
   * Is used to register a new hook
   * @param hook The hook that is to be registered
   */
  public void registerHook(Hook hook) {
    injector.getInstance(HookRegistry.class).register(hook);
  }

  /**
   * Can be called to unregister a hook
   * @param hook The hook that is to be unregistered
   */
  public void unregisterHook(Hook hook) {
    injector.getInstance(HookRegistry.class).unregister(hook);
  }
}
