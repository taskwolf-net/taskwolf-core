package com.dulno.core.workflow;

import com.beust.jcommander.internal.Lists;
import com.dulno.core.action.ActionDatabaseTable;
import com.dulno.core.action.ActionEntry;
import com.dulno.core.bundle.Bundle;
import com.dulno.core.condition.ConditionDatabaseTable;
import com.dulno.core.condition.ConditionEntry;
import com.dulno.core.condition.ConditionFactory;
import com.dulno.core.error.ErrorRepository;
import com.dulno.core.loop.LoopDatabaseTable;
import com.dulno.core.loop.LoopEntry;
import com.dulno.core.loop.LoopFactory;
import com.dulno.core.module.ModuleLoader;
import com.dulno.core.organization.team.Team;
import com.dulno.core.workflow.step.WorkflowStep;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.dulno.core.bundle.BundleDatabaseTable;
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
    return findWorkflowBundleOwner(workflowEntry)
      .thenCompose(bundleOwner -> bundleDatabaseTable.findBundle(bundleOwner)
        .thenCompose(bundle -> assembleWorkflowSteps(workflowEntry, bundle)
          .thenApply(steps -> assemblyWorkflow(workflowEntry, steps, bundle))));
  }

  private CompletableFuture<List<WorkflowStep>> assembleWorkflowSteps(
    WorkflowEntry workflowEntry, Bundle bundle
  ) {
    return actionDatabaseTable.findActionsByWorkflow(workflowEntry.id())
      .thenCompose(actions ->
        conditionDatabaseTable.findConditionsByWorkflow(workflowEntry.id())
          .thenCompose(conditions -> findLoop(workflowEntry.id())
            .thenApplyAsync(loop -> assembleWorkflowSteps(actions, conditions,
              loop, bundle, 0))));
  }

  private List<WorkflowStep> assembleWorkflowSteps(
    List<ActionEntry> actions, List<ConditionEntry> conditions,
    Optional<LoopEntry> loop, Bundle bundle, int startingIndex
  ) {
    //TODO: DO IT PARALLEL FOR FASTER ASSEMBLY TIME
    var result = Lists.<WorkflowStep>newArrayList();
    var currentIndex = startingIndex;
    var currentStep = findWorkflowStep(actions, conditions, loop, bundle,
      currentIndex).join();
    while (currentStep != null) {
      result.add(currentStep);
      currentIndex++;
      currentStep = findWorkflowStep(actions, conditions, loop, bundle,
        currentIndex).join();
    }
    return result;
  }

  private CompletableFuture<WorkflowStep> findWorkflowStep(
    List<ActionEntry> actions, List<ConditionEntry> conditions,
    Optional<LoopEntry> loop, Bundle bundle, int index
  ) {
    var action = actions.stream().filter(entry -> entry.index() == index)
      .findFirst();
    if (action.isPresent()) {
      return prepareAction(action.get());
    }
    var condition = conditions.stream().filter(entry -> entry.index() == index)
      .findFirst();
    if (condition.isPresent()) {
      return prepareCondition(condition.get());
    }
    if (loop.isPresent() && loop.get().index() == index) {
      return prepareLoop(loop.get(), bundle, index, actions, conditions);
    }
    return CompletableFuture.completedFuture(null);
  }

  private CompletableFuture<WorkflowStep> prepareAction(ActionEntry entry) {
    var module = moduleLoader.findRegisteredModuleById(entry.module()).get();
    var action = module.module().actionRepository()
      .findAction(entry.type()).get();
    return (CompletableFuture<WorkflowStep>) action.build(entry.id());
  }

  private CompletableFuture<WorkflowStep> prepareCondition(ConditionEntry entry) {
    return CompletableFuture.completedFuture(conditionFactory.create(
      entry.type(), entry.content()));
  }

  private CompletableFuture<WorkflowStep> prepareLoop(
    LoopEntry entry, Bundle bundle, int index, List<ActionEntry> actions,
    List<ConditionEntry> conditions
  ) {
    var steps = assembleWorkflowSteps(actions, conditions, Optional.empty(),
      bundle, index);
    return CompletableFuture.completedFuture(loopFactory.create(entry.type(),
      entry.content(), steps, bundle));
  }

  private Workflow assemblyWorkflow(
    WorkflowEntry workflowEntry, List<WorkflowStep> steps, Bundle bundle
  ) {
    return Workflow.create(workflowDatabaseTable, timelineDatabaseTable,
      userDatabaseTable, operationDatabaseTable, workflowThrottleDatabaseTable,
      organizationDatabaseTable, notificationDatabaseTable, maintenanceSchedule,
      translation, errorRepository, notificationMail, workflowEntry, steps, bundle);
  }

  private CompletableFuture<Optional<LoopEntry>> findLoop(UUID workflowId) {
    return loopDatabaseTable.loopExistsByWorkflow(workflowId)
      .thenCompose(exists -> exists ?
        loopDatabaseTable.findLoopByWorkflow(workflowId).thenApply(Optional::of) :
        CompletableFuture.completedFuture(Optional.empty()));
  }

  private CompletableFuture<UUID> findWorkflowBundleOwner(WorkflowEntry workflow) {
    var owner = workflow.ownerId();
    return userDatabaseTable.userExists(owner)
      .thenCompose(userExists -> organizationDatabaseTable.organizationExists(owner)
        .thenCompose(organizationExists -> userExists || organizationExists ?
          CompletableFuture.completedFuture(owner) :
          teamDatabaseTable.findTeam(owner).thenApply(Team::organizationId)));
  }
}
