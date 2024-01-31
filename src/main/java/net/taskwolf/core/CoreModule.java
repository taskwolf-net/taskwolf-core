package net.taskwolf.core;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.taskwolf.core.action.Action;
import net.taskwolf.core.action.ActionDatabaseTable;
import net.taskwolf.core.action.ActionEntry;
import net.taskwolf.core.action.ActionInformation;
import net.taskwolf.core.condition.Condition;
import net.taskwolf.core.condition.ConditionDatabaseTable;
import net.taskwolf.core.condition.ConditionEntry;
import net.taskwolf.core.condition.ConditionFactory;
import net.taskwolf.core.distribution.Distribution;
import net.taskwolf.core.locale.Locale;
import net.taskwolf.core.module.Module;
import net.taskwolf.core.module.ModuleInformation;
import net.taskwolf.core.module.ModuleLoader;
import net.taskwolf.core.trigger.Trigger;
import net.taskwolf.core.trigger.TriggerDatabaseTable;
import net.taskwolf.core.trigger.TriggerEntry;
import net.taskwolf.core.trigger.TriggerInformation;
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

  public Optional<TriggerInformation> findTriggerInformation(
    String moduleName, String triggerType
  ) {
    var moduleOptional = moduleLoader.findModule(moduleName);
    if (moduleOptional.isEmpty()) {
      return Optional.empty();
    }
    var module = moduleOptional.get();
    return module.triggerInformation().stream().filter(triggerInformation ->
      triggerInformation.identifier().equals(triggerType)).findFirst();
  }

  public Optional<ActionInformation> findActionInformation(
    String moduleName, String actionType
  ) {
    var moduleOptional = moduleLoader.findModule(moduleName);
    if (moduleOptional.isEmpty()) {
      return Optional.empty();
    }
    var module = moduleOptional.get();
    return module.actionInformation().stream().filter(actionInformation ->
      actionInformation.identifier().equals(actionType)).findFirst();
  }

  public CompletableFuture<List<TriggerEntry>> findTriggerEntries(
    String module, String type
  ) {
    return triggerDatabaseTable.findTriggersByModuleAndType(module, type)
      .thenApply(entries -> entries.stream()
        .filter(entry -> distribution.isAssignedUser(module, entry.ownerId()))
        .filter(entry -> entry.state().isArmed()).toList());
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

  private CompletableFuture<Map<Integer, Action>> createActions(UUID workflowId) {
    return actionDatabaseTable.findActionsByWorkflow(workflowId)
      .thenApply(this::createActionsMap);
  }

  private Map<Integer, Action> createActionsMap(List<ActionEntry> actions) {
    var result = Maps.<Integer, Action>newHashMap();
    for (var action : actions) {
      result.put(action.actionIndex(), createAction(action.module(),
        action.type(), action.content()));
    }
    return result;
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

  public Trigger createTrigger(String module, String type, String content) {
    return moduleLoader.findModule(module).get().triggerFactory().create(type, content);
  }

  public Action createAction(String module, String type, String content) {
    return moduleLoader.findModule(module).get().actionFactory().create(type, content);
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
