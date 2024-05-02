package net.taskwolf.core;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.taskwolf.core.action.*;
import net.taskwolf.core.condition.Condition;
import net.taskwolf.core.condition.ConditionDatabaseTable;
import net.taskwolf.core.condition.ConditionEntry;
import net.taskwolf.core.condition.ConditionFactory;
import net.taskwolf.core.distribution.Distribution;
import net.taskwolf.core.iterator.AsyncIterator;
import net.taskwolf.core.locale.Locale;
import net.taskwolf.core.module.Module;
import net.taskwolf.core.module.ModuleInformation;
import net.taskwolf.core.module.ModuleLoader;
import net.taskwolf.core.trigger.*;
import net.taskwolf.core.user.User;
import net.taskwolf.core.user.UserDatabaseTable;
import net.taskwolf.core.workflow.Workflow;
import net.taskwolf.core.workflow.WorkflowDatabaseTable;
import net.taskwolf.core.workflow.WorkflowFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Singleton
public class CoreModule {
  private final ModuleLoader moduleLoader;
  private final UserDatabaseTable userDatabaseTable;
  private final TriggerDatabaseTable triggerDatabaseTable;
  private final ActionDatabaseTable actionDatabaseTable;
  private final ConditionDatabaseTable conditionDatabaseTable;
  private final WorkflowDatabaseTable workflowDatabaseTable;
  private final Distribution distribution;
  private final ConditionFactory conditionFactory;
  private final WorkflowFactory workflowFactory;
  private final Locale englishLocale;
  private final Locale germanLocale;

  @Inject
  private CoreModule(
    ModuleLoader moduleLoader, UserDatabaseTable userDatabaseTable,
    TriggerDatabaseTable triggerDatabaseTable,
    ActionDatabaseTable actionDatabaseTable,
    ConditionDatabaseTable conditionDatabaseTable,
    WorkflowDatabaseTable workflowDatabaseTable,
    Distribution distribution, ConditionFactory conditionFactory,
    WorkflowFactory workflowFactory, @Named("englishLocale") Locale englishLocale,
    @Named("germanLocale") Locale germanLocale
  ) {
    this.moduleLoader = moduleLoader;
    this.userDatabaseTable = userDatabaseTable;
    this.triggerDatabaseTable = triggerDatabaseTable;
    this.actionDatabaseTable = actionDatabaseTable;
    this.conditionDatabaseTable = conditionDatabaseTable;
    this.workflowDatabaseTable = workflowDatabaseTable;
    this.distribution = distribution;
    this.conditionFactory = conditionFactory;
    this.workflowFactory = workflowFactory;
    this.englishLocale = englishLocale;
    this.germanLocale = germanLocale;
  }

  void initialize() throws Exception {
    moduleLoader.loadModules();
  }

  public Optional<ModuleInformation> findModuleInformation(String moduleName) {
    var moduleOptional = moduleLoader.findModule(moduleName);
    if (moduleOptional.isEmpty()) {
      return Optional.empty();
    }
    return moduleOptional.map(Module::moduleInformation);
  }

  public Optional<Trigger> findTrigger(String moduleName, String triggerType) {
    var moduleOptional = moduleLoader.findModule(moduleName);
    if (moduleOptional.isEmpty()) {
      return Optional.empty();
    }
    var module = moduleOptional.get();
    return module.triggerRepository().findTrigger(triggerType);
  }

  public Optional<Action<? extends ActionExecutor>> findAction(
    String moduleName, String actionType
  ) {
    var moduleOptional = moduleLoader.findModule(moduleName);
    if (moduleOptional.isEmpty()) {
      return Optional.empty();
    }
    var module = moduleOptional.get();
    return module.actionRepository().findAction(actionType);
  }

  public Optional<TriggerInformation> findTriggerInformation(
    String moduleName, String triggerType
  ) {
    return findTrigger(moduleName, triggerType).map(Trigger::information);
  }

  public Optional<ActionInformation> findActionInformation(
    String moduleName, String triggerType
  ) {
    return findAction(moduleName, triggerType).map(Action::information);
  }

  public void triggerWorkflows(
    String moduleName, String triggerType, String condition,
    Map<String, Object> information
  ) {
    triggerWorkflows(moduleName, triggerType, condition, information, true);
  }

  public void triggerWorkflows(
    String moduleName, String triggerType, String condition,
    Map<String, Object> information, boolean checkDistribution
  ) {
    var module = moduleLoader.findModule(moduleName).get();
    var trigger = module.triggerRepository()
      .findTrigger(triggerType).get();
    if (!condition.isEmpty()) {
      condition = " WHERE " + condition + " ALLOW FILTERING";
    }
    trigger.findEntries(condition).thenAccept(triggers ->
      buildWorkflowTriggers(triggers, moduleName, information, checkDistribution));
  }

  private void buildWorkflowTriggers(
    List<UUID> triggerIds, String moduleName, Map<String, Object> information,
    boolean checkDistribution
  ) {
    AsyncIterator.execute(triggerIds, triggerDatabaseTable::findTrigger,
      triggerIds.size(), triggers ->
        filterTriggerEntries(triggers, moduleName, checkDistribution)
          .forEach(entry -> createWorkflow(entry.id())
            .thenAccept(workflow -> workflow.trigger(information))));
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

  public CompletableFuture<Workflow> createWorkflow(UUID triggerId) {
    var futureResponse = new CompletableFuture<Workflow>();
    workflowDatabaseTable.findWorkflowByTrigger(triggerId).thenAccept(workflowEntry ->
      createActions(workflowEntry.id()).thenApply(actions ->
        createConditions(workflowEntry.id()).thenApply(conditions ->
          futureResponse.complete(workflowFactory.create(workflowEntry, actions,
            conditions)))));
    return futureResponse;
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
        .thenAccept(action -> result.put(entry.actionIndex(), action)),
      actions.size(), value -> futureResponse.complete(result));
    return futureResponse;
  }

  public CompletableFuture<ActionExecutor> createAction(
    String moduleName, String actionType, UUID actionId
  ) {
    var module = moduleLoader.findModule(moduleName).get();
    var action = module.actionRepository()
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

  public CompletableFuture<String> translate(UUID userId, String key) {
    return userDatabaseTable.findUser(userId).thenApply(user -> translate(user, key));
  }

  public String translate(User user, String key) {
    return translate(user.language(), key);
  }

  public String translate(String language, String key) {
    return switch(language.toLowerCase()) {
      case "en" -> englishLocale.findText(key);
      case "de" -> germanLocale.findText(key);
      default -> "LANGUAGE NOT FOUND";
    };
  }
}
