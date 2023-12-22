package net.taskwolf.core;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.action.Action;
import net.taskwolf.core.action.ActionDatabaseTable;
import net.taskwolf.core.action.ActionInformation;
import net.taskwolf.core.command.CommandRegistry;
import net.taskwolf.core.condition.*;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.distribution.Distribution;
import net.taskwolf.core.iterator.AsyncAllocationIterator;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.module.Module;
import net.taskwolf.core.module.ModuleInformation;
import net.taskwolf.core.module.ModuleLoader;
import net.taskwolf.core.organization.InvitationDatabaseTable;
import net.taskwolf.core.organization.OrganizationDatabaseTable;
import net.taskwolf.core.template.TemplateDatabaseTable;
import net.taskwolf.core.trigger.Trigger;
import net.taskwolf.core.trigger.TriggerDatabaseTable;
import net.taskwolf.core.trigger.TriggerEntry;
import net.taskwolf.core.trigger.TriggerInformation;
import net.taskwolf.core.user.UserDatabaseTable;
import net.taskwolf.core.workflow.Workflow;
import net.taskwolf.core.workflow.WorkflowDatabaseTable;
import net.taskwolf.core.workflow.WorkflowExecutionDatabaseTable;
import org.springframework.boot.SpringApplication;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public class CoreModule {
  private final Log log;
  private final ModuleLoader moduleLoader;
  private final DatabaseConnection databaseConnection;
  private final DatabaseKeyspace databaseKeyspace;
  private final UserDatabaseTable userDatabaseTable;
  private final OrganizationDatabaseTable organizationDatabaseTable;
  private final InvitationDatabaseTable invitationDatabaseTable;
  private final TriggerDatabaseTable triggerDatabaseTable;
  private final ActionDatabaseTable actionDatabaseTable;
  private final ConditionDatabaseTable conditionDatabaseTable;
  private final WorkflowDatabaseTable workflowDatabaseTable;
  private final WorkflowExecutionDatabaseTable workflowExecutionDatabaseTable;
  private final TemplateDatabaseTable templateDatabaseTable;
  private final Distribution distribution;
  private final CommandRegistry commandRegistry;
  private final ConditionFactory conditionFactory;
  private final ConditionInformationRepository conditionRepository;
  private final SpringApplication springApplication;

  void initialize() throws Exception {
    moduleLoader.loadModules(this);
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
    var futureResponse = new CompletableFuture<List<TriggerEntry>>();
    triggerDatabaseTable.findTriggersByModuleAndType(module, type)
      .thenApply(entries -> AsyncAllocationIterator.execute(entries, entry ->
          distribution.isAssignedUser(module, entry.ownerId()), entries.size(),
        triggers -> futureResponse.complete(triggers.entrySet().stream()
          .filter(Map.Entry::getValue).map(Map.Entry::getKey)
          .filter(entry -> entry.state().isArmed()).toList())));
    return futureResponse;
  }

  public CompletableFuture<Workflow> createWorkflow(UUID triggerId) {
    var futureResponse = new CompletableFuture<Workflow>();
    workflowDatabaseTable.findWorkflowByTrigger(triggerId).thenAccept(workflowEntry ->
      createActions(workflowEntry.id()).thenApply(actions ->
        createConditions(workflowEntry.id()).thenApply(conditions ->
          futureResponse.complete(Workflow.create(workflowExecutionDatabaseTable,
            workflowEntry.id(), actions, conditions)))));
    return futureResponse;
  }

  private CompletableFuture<List<Action>> createActions(UUID workflowId) {
    return actionDatabaseTable.findActionsByWorkflow(workflowId)
      .thenApply(entries -> entries.stream().map(entry ->
        createAction(entry.module(), entry.type(), entry.content()))
        .collect(Collectors.toList()));
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
}
