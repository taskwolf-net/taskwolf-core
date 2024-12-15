package com.dulno.core.workflow;

import com.dulno.core.action.ActionDatabaseTable;
import com.dulno.core.action.ActionEntry;
import com.dulno.core.condition.ConditionDatabaseTable;
import com.dulno.core.condition.ConditionEntry;
import com.dulno.core.condition.ConditionFactory;
import com.dulno.core.error.ErrorRepository;
import com.dulno.core.iterator.AsyncIterator;
import com.dulno.core.loop.Loop;
import com.dulno.core.loop.LoopDatabaseTable;
import com.dulno.core.loop.LoopEntry;
import com.dulno.core.loop.LoopFactory;
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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Singleton
public final class WorkflowFactory {
  private final WorkflowDatabaseTable workflowDatabaseTable;
  private final ActionDatabaseTable actionDatabaseTable;
  private final ConditionDatabaseTable conditionDatabaseTable;
  private final ConditionFactory conditionFactory;
  private final LoopDatabaseTable loopDatabaseTable;
  private final LoopFactory loopFactory;
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
    LoopDatabaseTable loopDatabaseTable, LoopFactory loopFactory,
    TimelineDatabaseTable timelineDatabaseTable,
    UserDatabaseTable userDatabaseTable,
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
    this.loopDatabaseTable = loopDatabaseTable;
    this.loopFactory = loopFactory;
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
    return actionDatabaseTable.findActionsByWorkflow(workflowEntry.id())
      .thenCompose(actions ->
        conditionDatabaseTable.findConditionsByWorkflow(workflowEntry.id())
          .thenCompose(conditions -> findLoop(workflowEntry.id())
            .thenCompose(loopEntry -> processWorkflowLoop(workflowEntry,
              actions, conditions, loopEntry))));
  }

  private CompletableFuture<Workflow> processWorkflowLoop(
    WorkflowEntry workflowEntry, List<ActionEntry> actions,
    List<ConditionEntry> conditions, Optional<LoopEntry> loopEntryOptional
  ) {
    if (loopEntryOptional.isEmpty()) {
      return constructWorkflow(workflowEntry, actions, conditions, Optional.empty());
    }
    var loopEntry = loopEntryOptional.get();
    var workflowActions = actions.stream()
      .filter(action -> loopEntry.actionIds().stream()
        .noneMatch(entry -> entry.equals(action.id()))).toList();
    var workflowConditions = conditions.stream()
      .filter(condition -> loopEntry.conditionIds().stream()
        .noneMatch(entry -> entry.equals(condition.id()))).toList();
    return createLoop(actions, conditions, loopEntry)
      .thenCompose(loop -> constructWorkflow(workflowEntry, workflowActions,
        workflowConditions, Optional.of(loop)));
  }

  private CompletableFuture<Workflow> constructWorkflow(
    WorkflowEntry workflowEntry, List<ActionEntry> actions,
    List<ConditionEntry> conditions, Optional<Loop> loop
  ) {
    return createActionsMap(actions)
      .thenApply(actionsMap -> assemblyWorkflow(workflowEntry, actionsMap,
        createConditionsMap(conditions), loop));
  }

  private Workflow assemblyWorkflow(
    WorkflowEntry workflowEntry, Map<Integer, ActionExecutor> actions,
    Multimap<Integer, Condition> conditions, Optional<Loop> loop
  ) {
    return Workflow.create(workflowDatabaseTable, timelineDatabaseTable,
      userDatabaseTable, bundleDatabaseTable, operationDatabaseTable,
      workflowThrottleDatabaseTable, organizationDatabaseTable, teamDatabaseTable,
      notificationDatabaseTable, maintenanceSchedule, translation,
      errorRepository, notificationMail, workflowEntry, actions, conditions,
      loop);
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

  private CompletableFuture<ActionExecutor> createAction(
    String moduleName, String actionType, UUID actionId
  ) {
    var module = moduleLoader.findRegisteredModuleById(moduleName).get();
    var action = module.module().actionRepository()
      .findAction(actionType).get();
    return (CompletableFuture<ActionExecutor>) action.build(actionId);
  }

  private Multimap<Integer, Condition> createConditionsMap(
    List<ConditionEntry> conditions
  ) {
    var result = HashMultimap.<Integer, Condition>create();
    for (var condition : conditions) {
      result.put(condition.actionIndex(), conditionFactory.create(condition.type(),
        condition.content()));
    }
    return result;
  }

  private CompletableFuture<Optional<LoopEntry>> findLoop(UUID workflowId) {
    return loopDatabaseTable.loopExistsByWorkflow(workflowId)
      .thenCompose(exists -> exists ?
        loopDatabaseTable.findLoopByWorkflow(workflowId).thenApply(Optional::of) :
        CompletableFuture.completedFuture(Optional.empty()));
  }

  private CompletableFuture<Loop> createLoop(
    List<ActionEntry> actions, List<ConditionEntry> conditions, LoopEntry loopEntry
  ) {
    var loopActions = actions.stream()
      .filter(action -> loopEntry.actionIds().stream()
        .anyMatch(entry -> entry.equals(action.id()))).toList();
    var loopConditions = conditions.stream()
      .filter(condition -> loopEntry.conditionIds().stream()
        .anyMatch(entry -> entry.equals(condition.id()))).toList();
    return createActionsMap(loopActions)
      .thenApply(loopActionsMap -> loopFactory.create(loopEntry.type(),
        loopEntry.content(), loopActionsMap, createConditionsMap(loopConditions)));
  }
}
