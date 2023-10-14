package de.flexpedite.core;

import de.flexpedite.core.action.Action;
import de.flexpedite.core.action.ActionDatabaseTable;
import de.flexpedite.core.database.DatabaseConnection;
import de.flexpedite.core.database.DatabaseKeyspace;
import de.flexpedite.core.module.ModuleLoader;
import de.flexpedite.core.trigger.Trigger;
import de.flexpedite.core.trigger.TriggerDatabaseTable;
import de.flexpedite.core.workflow.Workflow;
import de.flexpedite.core.workflow.WorkflowDatabaseTable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public class CoreModule {
  private final ModuleLoader moduleLoader;
  @Getter
  private final DatabaseConnection databaseConnection;
  @Getter
  private final DatabaseKeyspace databaseKeyspace;
  @Getter
  private final TriggerDatabaseTable triggerDatabaseTable;
  @Getter
  private final ActionDatabaseTable actionDatabaseTable;
  private final WorkflowDatabaseTable workflowDatabaseTable;

  void initialize() throws Exception {
    moduleLoader.loadModules(this);
  }

  public CompletableFuture<Workflow> createWorkflow(UUID triggerId) {
    var futureResponse = new CompletableFuture<Workflow>();
    workflowDatabaseTable.findWorkflowByTrigger(triggerId).thenAccept(workflowEntry ->
      createActions(workflowEntry.id()).thenApply(actions ->
        futureResponse.complete(Workflow.create(actions))));
    return futureResponse;
  }

  private CompletableFuture<List<Action>> createActions(UUID workflowId) {
    return actionDatabaseTable.findActionsByWorkflow(workflowId)
      .thenApply(entries -> entries.stream().map(entry ->
        createAction(entry.module(), entry.content())).collect(Collectors.toList()));
  }

  public Trigger createTrigger(String module, String content) {
    return moduleLoader.findModule(module).triggerFactory().create(content);
  }

  public Action createAction(String module, String content) {
    return moduleLoader.findModule(module).actionFactory().create(content);
  }
}
