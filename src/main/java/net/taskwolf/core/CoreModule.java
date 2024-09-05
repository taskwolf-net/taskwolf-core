package net.taskwolf.core;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.taskwolf.core.action.*;
import net.taskwolf.core.condition.Condition;
import net.taskwolf.core.condition.ConditionDatabaseTable;
import net.taskwolf.core.condition.ConditionEntry;
import net.taskwolf.core.condition.ConditionFactory;
import net.taskwolf.core.database.condition.DatabaseCondition;
import net.taskwolf.core.iterator.AsyncIterator;
import net.taskwolf.core.locale.Translation;
import net.taskwolf.core.module.Module;
import net.taskwolf.core.module.ModuleInformation;
import net.taskwolf.core.module.ModuleLoader;
import net.taskwolf.core.module.RegisteredModule;
import net.taskwolf.core.trigger.Trigger;
import net.taskwolf.core.trigger.TriggerDatabaseTable;
import net.taskwolf.core.trigger.TriggerEntry;
import net.taskwolf.core.trigger.TriggerInformation;
import net.taskwolf.core.user.User;
import net.taskwolf.core.worker.WorkerDistribution;
import net.taskwolf.core.workflow.Workflow;
import net.taskwolf.core.workflow.WorkflowDatabaseTable;
import net.taskwolf.core.workflow.WorkflowEntry;
import net.taskwolf.core.workflow.WorkflowFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Singleton
public class CoreModule {
  private final ModuleLoader moduleLoader;
  private final TriggerDatabaseTable triggerDatabaseTable;
  private final ActionDatabaseTable actionDatabaseTable;
  private final ConditionDatabaseTable conditionDatabaseTable;
  private final WorkflowDatabaseTable workflowDatabaseTable;
  private final WorkerDistribution distribution;
  private final ConditionFactory conditionFactory;
  private final WorkflowFactory workflowFactory;
  private final Translation translation;

  @Inject
  private CoreModule(
    ModuleLoader moduleLoader, TriggerDatabaseTable triggerDatabaseTable,
    ActionDatabaseTable actionDatabaseTable,
    ConditionDatabaseTable conditionDatabaseTable,
    WorkflowDatabaseTable workflowDatabaseTable,
    WorkerDistribution distribution, ConditionFactory conditionFactory,
    WorkflowFactory workflowFactory, Translation translation
  ) {
    this.moduleLoader = moduleLoader;
    this.triggerDatabaseTable = triggerDatabaseTable;
    this.actionDatabaseTable = actionDatabaseTable;
    this.conditionDatabaseTable = conditionDatabaseTable;
    this.workflowDatabaseTable = workflowDatabaseTable;
    this.distribution = distribution;
    this.conditionFactory = conditionFactory;
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
    return findAllTriggerEntries(module, type, true);
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
    var futureResponse = new CompletableFuture<List<TriggerEntry>>();
    findTrigger(module, type).get().findEntries(DatabaseCondition.empty())
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
    return createActions(workflowEntry.id()).thenCompose(actions ->
      createConditions(workflowEntry.id()).thenApply(conditions ->
        workflowFactory.create(workflowEntry, actions, conditions)));
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
