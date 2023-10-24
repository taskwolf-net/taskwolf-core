package net.taskwolf.core.template;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class TemplateDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "templates";

  public static TemplateDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("trigger", DatabaseDataType.TEXT));
    columns.add(DatabaseListColumn.create("actions", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("name", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("description", DatabaseDataType.TEXT));
    return new TemplateDatabaseTable(connection, keyspace, TABLE_NAME, columns);
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
      template.name(), template.description());
  }

  public void insertTemplate(
    UUID id, String trigger, List<String> actions, String name, String description
  ) {
    insert(DatabaseRow.of(id, trigger, actions, name, description));
  }

  public void deleteTemplate(UUID templateId) {
    delete(DatabaseCell.create(templateId));
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
    return exists(DatabaseCell.create(templateId));
  }

  public CompletableFuture<Template> findTemplate(UUID templateId) {
    return selectRow(DatabaseCell.create(templateId)).thenApply(Template::of);
  }

  public CompletableFuture<List<Template>> findTemplatesByModule(String module) {
    return selectRows("module='" + module + "'")
      .thenApply(rows -> rows.stream().map(Template::of)
        .collect(Collectors.toList()));
  }

  public CompletableFuture<List<Template>> findAllTemplates() {
    return selectAllRows().thenApply(rows -> rows.stream().map(Template::of)
      .collect(Collectors.toList()));
  }
}
