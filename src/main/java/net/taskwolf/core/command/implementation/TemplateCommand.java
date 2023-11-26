package net.taskwolf.core.command.implementation;

import com.google.common.collect.Lists;
import net.taskwolf.core.command.Command;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.template.Template;
import net.taskwolf.core.template.TemplateAction;
import net.taskwolf.core.template.TemplateDatabaseTable;
import net.taskwolf.core.template.TemplateTrigger;

import java.util.List;
import java.util.UUID;

public final class TemplateCommand extends Command {
  public static TemplateCommand create(
    Log log, TemplateDatabaseTable templateDatabaseTable
  ) {
    return new TemplateCommand(log, templateDatabaseTable);
  }

  private final TemplateDatabaseTable templateDatabaseTable;

  private TemplateCommand(
    Log log, TemplateDatabaseTable templateDatabaseTable
  ) {
    super(log, "template", new String[] {"templates"}, new String[] {"find <module>", "add <name, description, triggerModule, triggerDescription, [{actionModule, actionDescription}, ...]>", "remove <id>"});
    this.templateDatabaseTable = templateDatabaseTable;
  }

  @Override
  public boolean execute(String[] arguments) {
    if (arguments.length == 0) {
      return false;
    }
    if (arguments[0].equalsIgnoreCase("find")) {
      return findTemplates(arguments);
    }
    if (arguments[0].equalsIgnoreCase("add")) {
      return addTemplate(arguments);
    }
    if (arguments[0].equalsIgnoreCase("remove")) {
      return removeTemplate(arguments);
    }
    return false;
  }

  private boolean findTemplates(String[] arguments) {
    if (arguments.length != 2) {
      return false;
    }
    var module = arguments[1];
    templateDatabaseTable.findTemplatesByModule(module).thenAccept(templates ->
      printTemplates(module, templates));
    return true;
  }

  private void printTemplates(String module, List<Template> templates) {
    if (templates.isEmpty()) {
      log().info("No templates have been added for this module yet");
      return;
    }
    log().info("Templates for module " + module + " (" + templates.size() + "):");
    for (var template : templates) {
      var generalInformation = new StringBuilder(" - ");
      generalInformation.append(template.name());
      generalInformation.append(": ");
      generalInformation.append(template.description());
      generalInformation.append(" (");
      generalInformation.append(template.id());
      generalInformation.append(")");
      log().info(generalInformation.toString());
      var detailedInformation = new StringBuilder("   ");
      detailedInformation.append(template.trigger().module());
      for (var action : template.actions()) {
        detailedInformation.append(" -> ");
        detailedInformation.append(action.module());
      }
      log().info(detailedInformation.toString());
    }
  }

  private boolean addTemplate(String[] arguments) {
    if (arguments.length < 7 || (arguments.length & 1) == 0) {
      return false;
    }
    var name = arguments[1];
    var description = arguments[2];
    var triggerModule = arguments[3];
    var triggerType = arguments[4];
    var actionModules = Lists.<String>newArrayList();
    var actionTypes = Lists.<String>newArrayList();
    for (int i = 5; i < arguments.length; i += 2) {
      actionModules.add(arguments[i]);
      actionTypes.add(arguments[i + 1]);
    }
    templateDatabaseTable.generateAvailableTemplateId().thenAccept(id ->
      insertTemplate(id, name, description, triggerModule, triggerType,
        actionModules, actionTypes));
    return true;
  }

  private void insertTemplate(
    UUID id, String name, String description, String triggerModule,
    String triggerType, List<String> actionModules, List<String> actionTypes
  ) {
    var actions = Lists.<TemplateAction>newArrayList();
    for (int i = 0; i < actionTypes.size(); i++) {
      actions.add(TemplateAction.create(actionModules.get(i), actionTypes.get(i)));
    }
    templateDatabaseTable.insertTemplate(Template.create(id,
      TemplateTrigger.create(triggerModule, triggerType), actions, name, description));
    log().info("Successfully created Template " + id.toString());
  }

  private boolean removeTemplate(String[] arguments) {
    if (arguments.length != 2) {
      return false;
    }
    var id = UUID.fromString(arguments[1]);
    templateDatabaseTable.templateExists(id).thenAccept(exists ->
      deleteTemplate(id, exists));
    return true;
  }

  private void deleteTemplate(UUID id, boolean exists) {
    if (!exists) {
      log().info("Template with id" + id.toString() + " couldn't be found");
      return;
    }
    templateDatabaseTable.deleteTemplate(id);
    log().info("Successfully deleted Template " + id.toString());
  }
}
