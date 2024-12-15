package com.dulno.core;

import com.dulno.core.action.*;
import com.dulno.core.trigger.TriggerDatabaseTable;
import com.dulno.core.trigger.TriggerEntry;
import com.dulno.core.user.User;
import com.dulno.core.workflow.WorkflowFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.dulno.core.database.condition.DatabaseCondition;
import com.dulno.core.iterator.AsyncIterator;
import com.dulno.core.locale.Translation;
import com.dulno.core.module.Module;
import com.dulno.core.module.ModuleInformation;
import com.dulno.core.module.ModuleLoader;
import com.dulno.core.module.RegisteredModule;
import com.dulno.core.trigger.Trigger;
import com.dulno.core.trigger.TriggerInformation;
import com.dulno.core.worker.WorkerDistribution;
import com.dulno.core.workflow.Workflow;
import com.dulno.core.workflow.WorkflowDatabaseTable;
import com.dulno.core.workflow.WorkflowEntry;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Singleton
public class CoreModule {
  private final ModuleLoader moduleLoader;
  private final TriggerDatabaseTable triggerDatabaseTable;
  private final WorkflowDatabaseTable workflowDatabaseTable;
  private final WorkerDistribution distribution;
  private final WorkflowFactory workflowFactory;
  private final Translation translation;

  @Inject
  private CoreModule(
    ModuleLoader moduleLoader, TriggerDatabaseTable triggerDatabaseTable,
    WorkflowDatabaseTable workflowDatabaseTable,
    WorkerDistribution distribution, WorkflowFactory workflowFactory,
    Translation translation
  ) {
    this.moduleLoader = moduleLoader;
    this.triggerDatabaseTable = triggerDatabaseTable;
    this.workflowDatabaseTable = workflowDatabaseTable;
    this.distribution = distribution;
    this.workflowFactory = workflowFactory;
    this.translation = translation;
  }

  /**
   * Initializes the core module (loads all modules)
   * @throws Exception
   */
  public void initialize() throws Exception {
    moduleLoader.loadModules();
  }

  /**
   * Is used to find module information
   * @param moduleName The name of the module
   * @return The information of the module
   */
  public Optional<ModuleInformation> findModuleInformation(String moduleName) {
    var moduleOptional = moduleLoader.findRegisteredModuleById(moduleName);
    if (moduleOptional.isEmpty()) {
      return Optional.empty();
    }
    return moduleOptional.map(RegisteredModule::module).map(Module::moduleInformation);
  }

  /**
   * Is used to find a trigger
   * @param moduleName The name of the module in which the trigger is located
   * @param triggerType The type of the trigger
   * @return The trigger if it could be found
   */
  public Optional<Trigger> findTrigger(String moduleName, String triggerType) {
    var moduleOptional = moduleLoader.findRegisteredModuleById(moduleName);
    if (moduleOptional.isEmpty()) {
      return Optional.empty();
    }
    var module = moduleOptional.get();
    return module.module().triggerRepository().findTrigger(triggerType);
  }

  /**
   * Is used to find an action
   * @param moduleName The name of the module in which the action is located
   * @param actionType The type of the action
   * @return The action if it could be found
   */
  public Optional<Action<? extends ActionExecutor>> findAction(
    String moduleName, String actionType
  ) {
    var moduleOptional = moduleLoader.findRegisteredModuleById(moduleName);
    if (moduleOptional.isEmpty()) {
      return Optional.empty();
    }
    var module = moduleOptional.get();
    return module.module().actionRepository().findAction(actionType);
  }

  /**
   * Is used to find the information of a trigger
   * @param moduleName The name of the module in which the trigger is located
   * @param triggerType The type of the trigger
   * @return The information of the trigger if it could be found
   */
  public Optional<TriggerInformation> findTriggerInformation(
    String moduleName, String triggerType
  ) {
    return findTrigger(moduleName, triggerType).map(Trigger::information);
  }

  /**
   * Is used to find the information of an action
   * @param moduleName The name of the module in which the action is located
   * @param actionType The type of the action
   * @return The information of the action if it could be found
   */
  public Optional<ActionInformation> findActionInformation(
    String moduleName, String actionType
  ) {
    return findAction(moduleName, actionType).map(Action::information);
  }

  /**
   * Triggers a workflow
   * @param moduleName The name of the module in which the trigger is located
   * @param triggerType The type of the trigger
   * @param condition The condition for trigger selection
   * @param information The trigger information
   */
  public void triggerWorkflows(
    String moduleName, String triggerType, DatabaseCondition condition,
    Map<String, Object> information
  ) {
    triggerWorkflows(moduleName, triggerType, condition, information, true);
  }

  /**
   * Triggers a workflow
   * @param moduleName The name of the module in which the trigger is located
   * @param triggerType The type of the trigger
   * @param condition The condition for trigger selection
   * @param information The trigger information
   * @param checkDistribution If true there is a distribution check,
   *                          if false there is no distribution check
   */
  public void triggerWorkflows(
    String moduleName, String triggerType, DatabaseCondition condition,
    Map<String, Object> information, boolean checkDistribution
  ) {
    var module = moduleLoader.findRegisteredModuleById(moduleName).get();
    var trigger = module.module().triggerRepository()
      .findTrigger(triggerType).get();
    trigger.findEntries(condition).thenAccept(triggers ->
      buildWorkflowTriggers(triggers, moduleName, information, checkDistribution));
  }

  private void buildWorkflowTriggers(
    List<UUID> triggerIds, String moduleName, Map<String, Object> information,
    boolean checkDistribution
  ) {
    AsyncIterator.execute(triggerIds, triggerDatabaseTable::findTrigger)
      .thenAccept(triggers ->
        filterTriggerEntries(triggers, moduleName, checkDistribution)
          .forEach(entry -> createWorkflow(entry.id())
            .thenAccept(workflow -> workflow.trigger(information))));
  }

  /**
   * Is used to find all triggers of one kind
   * @param module The name of the module in which the triggers are located
   * @param type The type of the trigger
   * @return A future that contains the list of trigger entries
   */
  public CompletableFuture<List<TriggerEntry>> findAllTriggerEntries(
    String module, String type
  ) {
    return findSomeTriggerEntries(module, type, DatabaseCondition.empty(), true);
  }

  /**
   * Is used to find all triggers of one kind
   * @param module The name of the module in which the triggers are located
   * @param type The type of the trigger
   * @param checkDistribution If true there is a distribution check,
   *                          if false there is no distribution check
   * @return A future that contains the list of trigger entries
   */
  public CompletableFuture<List<TriggerEntry>> findAllTriggerEntries(
    String module, String type, boolean checkDistribution
  ) {
    return findSomeTriggerEntries(module, type, DatabaseCondition.empty(),
      checkDistribution);
  }

  /**
   * Is used to find some triggers of one kind
   * @param module The name of the module in which the triggers are located
   * @param type The type of the trigger
   * @param condition The condition with that the triggers are found
   * @return A future that contains the list of trigger entries
   */
  public CompletableFuture<List<TriggerEntry>> findSomeTriggerEntries(
    String module, String type, DatabaseCondition condition
  ) {
    return findSomeTriggerEntries(module, type, condition, true);
  }

  /**
   * Is used to find all triggers of one kind
   * @param module The name of the module in which the triggers are located
   * @param type The type of the trigger
   * @param condition The condition with that the triggers are found
   * @param checkDistribution If true there is a distribution check,
   *                          if false there is no distribution check
   * @return A future that contains the list of trigger entries
   */
  public CompletableFuture<List<TriggerEntry>> findSomeTriggerEntries(
    String module, String type, DatabaseCondition condition,
    boolean checkDistribution
  ) {
    var futureResponse = new CompletableFuture<List<TriggerEntry>>();
    findTrigger(module, type).get().findEntries(condition)
      .thenAccept(entries -> AsyncIterator.execute(entries,
          triggerDatabaseTable::findTrigger)
        .thenAccept(triggers -> futureResponse.complete(
          filterTriggerEntries(triggers, module, checkDistribution))));
    return futureResponse;
  }

  private List<TriggerEntry> filterTriggerEntries(
    List<TriggerEntry> entries, String module, boolean checkDistribution
  ) {
    var stream = entries.stream();
    if (checkDistribution) {
      stream = stream.filter(entry ->
        distribution.isAssignedUser(module, entry.ownerId()));
    }
    stream = stream.filter(entry -> entry.state().isArmed());
    return stream.toList();
  }

  /**
   * Creates a workflow by trigger id
   * @param triggerId The id of the trigger
   * @return A future that contains the workflow
   */
  public CompletableFuture<Workflow> createWorkflow(UUID triggerId) {
    return workflowDatabaseTable.findWorkflowByTrigger(triggerId)
      .thenCompose(this::createWorkflow);
  }

  /**
   * Creates a workflow by workflow id
   * @param workflowId The id of the workflow
   * @return A future that contains the workflow
   */
  public CompletableFuture<Workflow> createWorkflowById(UUID workflowId) {
    return workflowDatabaseTable.findWorkflow(workflowId)
      .thenCompose(this::createWorkflow);
  }

  /**
   * Creates a workflow by workflow entry
   * @param workflowEntry The workflow entry
   * @return A future that contains the workflow
   */
  public CompletableFuture<Workflow> createWorkflow(WorkflowEntry workflowEntry) {
    return workflowFactory.create(workflowEntry);
  }

  /**
   * Translates a locale for a user
   * @param userId The id of the user
   * @param key The key of the locale
   * @return A future that contains the translated locale
   */
  @Deprecated
  public CompletableFuture<String> translate(UUID userId, String key) {
    return translation.translate(userId, key);
  }

  /**
   * Translates a locale for a user
   * @param user The user
   * @param key The key of the locale
   * @return A future that contains the translated locale
   */
  @Deprecated
  public String translate(User user, String key) {
    return translation.translate(user, key);
  }

  /**
   * Translates a locale into a specific language
   * @param language The language
   * @param key The key of the locale
   * @return A future that contains the translated locale
   */
  @Deprecated
  public String translate(String language, String key) {
    return translation.translate(language, key);
  }
}
