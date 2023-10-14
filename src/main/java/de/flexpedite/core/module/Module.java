package de.flexpedite.core.module;

import de.flexpedite.core.CoreModule;
import de.flexpedite.core.action.ActionDatabaseTable;
import de.flexpedite.core.action.ActionFactory;
import de.flexpedite.core.action.ActionInformation;
import de.flexpedite.core.database.DatabaseConnection;
import de.flexpedite.core.database.DatabaseKeyspace;
import de.flexpedite.core.trigger.TriggerDatabaseTable;
import de.flexpedite.core.trigger.TriggerFactory;
import de.flexpedite.core.trigger.TriggerInformation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Getter(AccessLevel.PROTECTED)
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Module {
  private final CoreModule coreModule;

  public abstract void enable() throws Exception;
  public abstract void disable() throws Exception;
  public abstract TriggerFactory triggerFactory();
  public abstract ActionFactory actionFactory();
  public abstract List<TriggerInformation> triggerInformation();
  public abstract List<ActionInformation> actionInformation();

  protected DatabaseConnection databaseConnection() {
    return coreModule.databaseConnection();
  }

  protected DatabaseKeyspace databaseKeyspace() {
    return coreModule.databaseKeyspace();
  }

  protected TriggerDatabaseTable triggerDatabaseTable() {
    return coreModule.triggerDatabaseTable();
  }

  protected ActionDatabaseTable actionDatabaseTable() {
    return coreModule.actionDatabaseTable();
  }
}
