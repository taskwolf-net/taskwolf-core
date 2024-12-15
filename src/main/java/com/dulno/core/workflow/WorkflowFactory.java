package com.dulno.core.workflow;

import com.dulno.core.action.ActionDatabaseTable;
import com.dulno.core.action.ActionEntry;
import com.dulno.core.condition.ConditionDatabaseTable;
import com.dulno.core.condition.ConditionEntry;
import com.dulno.core.condition.ConditionFactory;
import com.dulno.core.error.ErrorRepository;
import com.dulno.core.iterator.AsyncIterator;
import com.dulno.core.module.ModuleLoader;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.dulno.core.action.ActionExecutor;
import com.dulno.core.bundle.BundleDatabaseTable;
import com.dulno.core.condition.Condition;
import com.dulno.core.locale.Translation;
import com.dulno.core.mail.Mail;
import com.dulno.core.maintenance.MaintenanceSchedule;
import com.dulno.core.notification.NotificationDatabaseTable;
import com.dulno.core.organization.OrganizationDatabaseTable;
import com.dulno.core.organization.team.TeamDatabaseTable;
import com.dulno.core.user.UserDatabaseTable;
import com.dulno.core.workflow.operation.OperationDatabaseTable;
import com.dulno.core.workflow.throttle.WorkflowThrottleDatabaseTable;
import com.dulno.core.workflow.timeline.TimelineDatabaseTable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Singleton
public final class WorkflowFactory {
  private final WorkflowDatabaseTable workflowDatabaseTable;
  private final ActionDatabaseTable actionDatabaseTable;
  private final ConditionDatabaseTable conditionDatabaseTable;
  private final ConditionFactory conditionFactory;
  private final ModuleLoader moduleLoader;
  private final TimelineDatabaseTable timelineDatabaseTable;
  private final UserDatabaseTable userDatabaseTable;
  private final BundleDatabaseTable bundleDatabaseTable;
  private final OperationDatabaseTable operationDatabaseTable;
  private final WorkflowThrottleDatabaseTable workflowThrottleDatabaseTable;
  private final OrganizationDatabaseTable organizationDatabaseTable;
  private final TeamDatabaseTable teamDatabaseTable;
  private final NotificationDatabaseTable notificationDatabaseTable;
  private final MaintenanceSchedule maintenanceSchedule;
  private final Translation translation;
  private final ErrorRepository errorRepository;
  private final Mail notificationMail;

  @Inject
  private WorkflowFactory(
    WorkflowDatabaseTable workflowDatabaseTable,
    ActionDatabaseTable actionDatabaseTable,
    ConditionDatabaseTable conditionDatabaseTable,
    ConditionFactory conditionFactory, ModuleLoader moduleLoader,
    TimelineDatabaseTable timelineDatabaseTable, UserDatabaseTable userDatabaseTable,
    BundleDatabaseTable bundleDatabaseTable,
    OperationDatabaseTable operationDatabaseTable,
    WorkflowThrottleDatabaseTable workflowThrottleDatabaseTable,
    OrganizationDatabaseTable organizationDatabaseTable,
    TeamDatabaseTable teamDatabaseTable,
    NotificationDatabaseTable notificationDatabaseTable,
    MaintenanceSchedule maintenanceSchedule, Translation translation,
    ErrorRepository errorRepository,
    @Named("notificationMail") Mail notificationMail
  ) {
    this.workflowDatabaseTable = workflowDatabaseTable;
    this.actionDatabaseTable = actionDatabaseTable;
    this.conditionDatabaseTable = conditionDatabaseTable;
    this.conditionFactory = conditionFactory;
    this.moduleLoader = moduleLoader;
    this.timelineDatabaseTable = timelineDatabaseTable;
    this.userDatabaseTable = userDatabaseTable;
    this.bundleDatabaseTable = bundleDatabaseTable;
    this.operationDatabaseTable = operationDatabaseTable;
    this.workflowThrottleDatabaseTable = workflowThrottleDatabaseTable;
    this.organizationDatabaseTable = organizationDatabaseTable;
    this.teamDatabaseTable = teamDatabaseTable;
    this.notificationDatabaseTable = notificationDatabaseTable;
    this.maintenanceSchedule = maintenanceSchedule;
    this.translation = translation;
    this.errorRepository = errorRepository;
    this.notificationMail = notificationMail;
  }

  public CompletableFuture<Workflow> create(WorkflowEntry workflowEntry) {
    return createActions(workflowEntry.id())
      .thenCompose(actions -> createConditions(workflowEntry.id())
        .thenApply(conditions -> create(workflowEntry, actions, conditions)));
  }

  private Workflow create(
    WorkflowEntry workflowEntry, Map<Integer, ActionExecutor> actions,
    Multimap<Integer, Condition> conditions
  ) {
    return Workflow.create(workflowDatabaseTable, timelineDatabaseTable,
      userDatabaseTable, bundleDatabaseTable, operationDatabaseTable,
      workflowThrottleDatabaseTable, organizationDatabaseTable, teamDatabaseTable,
      notificationDatabaseTable, maintenanceSchedule, translation,
      errorRepository, notificationMail, workflowEntry, actions, conditions);
  }

  private CompletableFuture<Map<Integer, ActionExecutor>> createActions(UUID workflowId) {
    return actionDatabaseTable.findActionsByWorkflow(workflowId)
      .thenCompose(this::createActionsMap);
  }

  private CompletableFuture<Map<Integer, ActionExecutor>> createActionsMap(
    List<ActionEntry> actions
  ) {
    var futureResponse = new CompletableFuture<Map<Integer, ActionExecutor>>();
    var result = Maps.<Integer, ActionExecutor>newHashMap();
    AsyncIterator.execute(actions, entry ->
        createAction(entry.module(), entry.type(), entry.id())
          .thenAccept(action -> result.put(entry.actionIndex(), action)))
      .thenAccept(value -> futureResponse.complete(result));
    return futureResponse;
  }

  /**
   * Creates an {@link ActionExecutor}
   * @param moduleName The name of the module in which the action is located
   * @param actionType The type of the action
   * @param actionId The id of the action
   * @return A future that contains the action executor
   */
  public CompletableFuture<ActionExecutor> createAction(
    String moduleName, String actionType, UUID actionId
  ) {
    var module = moduleLoader.findRegisteredModuleById(moduleName).get();
    var action = module.module().actionRepository()
      .findAction(actionType).get();
    return (CompletableFuture<ActionExecutor>) action.build(actionId);
  }

  private CompletableFuture<Multimap<Integer, Condition>> createConditions(UUID workflowId) {
    return conditionDatabaseTable.findConditionsByWorkflow(workflowId)
      .thenApply(this::createConditionsMap);
  }

  private Multimap<Integer, Condition> createConditionsMap(List<ConditionEntry> conditions) {
    var result = HashMultimap.<Integer, Condition>create();
    for (var condition : conditions) {
      result.put(condition.actionIndex(), conditionFactory.create(condition.type(),
        condition.content()));
    }
    return result;
  }
}
