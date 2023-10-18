package net.taskwolf.core.module;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.CoreModule;
import net.taskwolf.core.action.ActionDatabaseTable;
import net.taskwolf.core.action.ActionFactory;
import net.taskwolf.core.action.ActionInformation;
import net.taskwolf.core.command.Command;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.trigger.TriggerDatabaseTable;
import net.taskwolf.core.trigger.TriggerFactory;
import net.taskwolf.core.trigger.TriggerInformation;
import net.taskwolf.core.user.UserDatabaseTable;
import net.taskwolf.core.workflow.WorkflowDatabaseTable;

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

  public void registerCommand(Command command) {
    coreModule.commandRegistry().register(command);
  }

  public void unregisterCommand(Command command) {
    coreModule.commandRegistry().unregister(command);
  }

  public Log log() {
    return coreModule.log();
  }

  public List<TriggerInformation> triggerInformation() {
    return Lists.newArrayList();
  }

  public List<ActionInformation> actionInformation() {
    return Lists.newArrayList();
  }

  protected DatabaseConnection databaseConnection() {
    return coreModule.databaseConnection();
  }

  protected DatabaseKeyspace databaseKeyspace() {
    return coreModule.databaseKeyspace();
  }

  protected UserDatabaseTable userDatabaseTable() {
    return coreModule.userDatabaseTable();
  }

  protected TriggerDatabaseTable triggerDatabaseTable() {
    return coreModule.triggerDatabaseTable();
  }

  protected ActionDatabaseTable actionDatabaseTable() {
    return coreModule.actionDatabaseTable();
  }

  protected WorkflowDatabaseTable workflowDatabaseTable() {
    return coreModule.workflowDatabaseTable();
  }
}
