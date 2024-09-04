package net.taskwolf.core.template;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;
import net.taskwolf.core.database.condition.DatabaseComparison;
import net.taskwolf.core.database.condition.DatabaseCondition;
import net.taskwolf.core.database.paging.DatabaseDirection;
import net.taskwolf.core.database.paging.DatabaseOrder;
import net.taskwolf.core.database.paging.DatabasePage;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class TemplateDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "template";

  public static TemplateDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("placeholder", DatabaseDataType.TEXT,
      DatabaseColumn.Type.PARTITION_KEY));
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.CLUSTERING_KEY));
    columns.add(DatabaseColumn.create("trigger", DatabaseDataType.TEXT));
    columns.add(DatabaseListColumn.create("actions", DatabaseDataType.TEXT));
    columns.add(DatabaseListColumn.create("modules", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("englishName", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("englishDescription", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("germanName", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("germanDescription", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("accessType", DatabaseDataType.TEXT));
    var table = new TemplateDatabaseTable(connection, keyspace, TABLE_NAME, columns);
    table.createIfNotExists();
    table.createIndexIfNotExists("englishName",
      "'org.apache.cassandra.index.sasi.SASIIndex' WITH OPTIONS = " +
        "{'mode': 'CONTAINS', 'analyzer_class': " +
        "'org.apache.cassandra.index.sasi.analyzer.NonTokenizingAnalyzer', " +
        "'case_sensitive': 'false'}");
    table.createIndexIfNotExists("germanName",
      "'org.apache.cassandra.index.sasi.SASIIndex' WITH OPTIONS = " +
        "{'mode': 'CONTAINS', 'analyzer_class': " +
        "'org.apache.cassandra.index.sasi.analyzer.NonTokenizingAnalyzer', " +
        "'case_sensitive': 'false'}");
    return table;
  }

  private TemplateDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertTemplate(Template template) {
    insertTemplate(template.id(), template.trigger().encode(),
      template.actions().stream().map(TemplateAction::encode).toList(),
      template.modules(), template.englishName(), template.englishDescription(),
      template.germanName(), template.germanDescription(), template.accessType());
  }

  public void insertTemplate(
    UUID id, String trigger, List<String> actions, List<String> modules,
    String englishName, String englishDescription, String germanName,
    String germanDescription, TemplateAccessType accessType
  ) {
    insert(DatabaseRow.of(".", id, trigger, actions, modules, englishName,
      englishDescription, germanName, germanDescription, accessType.toString()));
  }

  public void updateTemplate(Template template) {
    updateTemplate(template.id(), template.trigger().encode(),
      template.actions().stream().map(TemplateAction::encode).toList(),
      template.modules(), template.englishName(), template.englishDescription(),
      template.germanName(), template.germanDescription(), template.accessType());
  }

  public void updateTemplate(
    UUID id, String trigger, List<String> actions, List<String> modules,
    String englishName, String englishDescription, String germanName,
    String germanDescription, TemplateAccessType accessType
  ) {
    update(DatabaseCondition.of("placeholder", ".", "id", id),
      DatabaseRow.of(".", id, trigger, actions, modules, englishName,
        englishDescription, germanName, germanDescription, accessType.toString()));
  }

  public void deleteTemplate(UUID templateId) {
    delete(DatabaseCondition.of("placeholder", ".", "id", templateId));
  }

  public CompletableFuture<UUID> generateAvailableTemplateId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    templateExists(id).thenApply(exists -> exists ?
      generateAvailableTemplateId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Boolean> templateExists(UUID templateId) {
    return exists(DatabaseCondition.of("placeholder", ".", "id", templateId));
  }

  public CompletableFuture<Template> findTemplate(UUID templateId) {
    return selectRow(DatabaseCondition.of("placeholder", ".", "id", templateId))
      .thenApply(Template::of);
  }

  private static final int PAGE_SIZE = 3 * 5;

  public CompletableFuture<DatabasePage<Template>> loadNextTemplatePage(
    String pageState, String search, String language
  ) {
    if (!search.isEmpty()) {
      var name = switch(language) {
        case "en" -> "englishName";
        case "de" -> "germanName";
        default -> "englishName";
      };
      var condition = DatabaseCondition.of(
        DatabaseComparison.create("placeholder", "."),
        DatabaseComparison.create(name, "%" + search + "%",
          DatabaseComparison.Type.LIKE));
      return selectRows(condition, PAGE_SIZE).thenApply(rows ->
        createTemplatePage(DatabasePage.create(rows, "", 1)));
    }
    return shiftPage(".", DatabaseCondition.empty(), DatabaseOrder.ASCENDING,
      PAGE_SIZE, pageState, DatabaseDirection.FORWARD, DatabaseDirection.FORWARD)
      .thenApply(this::createTemplatePage);
  }

  private DatabasePage<Template> createTemplatePage(DatabasePage<DatabaseRow> page) {
    return DatabasePage.create(page.content().stream().map(Template::of).toList(),
      page.pageState(), page.pageNumber());
  }
}
