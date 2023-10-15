package de.flexpedite.core.module;

import com.google.common.collect.Lists;
import de.flexpedite.core.CoreModule;
import de.flexpedite.core.action.ActionDatabaseTable;
import de.flexpedite.core.action.ActionFactory;
import de.flexpedite.core.action.ActionInformation;
import de.flexpedite.core.command.Command;
import de.flexpedite.core.database.DatabaseConnection;
import de.flexpedite.core.database.DatabaseKeyspace;
import de.flexpedite.core.log.Log;
import de.flexpedite.core.trigger.TriggerDatabaseTable;
import de.flexpedite.core.trigger.TriggerFactory;
import de.flexpedite.core.trigger.TriggerInformation;
import de.flexpedite.core.user.UserDatabaseTable;
import de.flexpedite.core.workflow.WorkflowDatabaseTable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.boot.SpringApplication;

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

  protected SpringApplication springApplication() {
    return coreModule.springApplication();
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
