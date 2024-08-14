package net.taskwolf.core.template;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.database.DatabaseRow;

import java.util.List;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class Template {
  public static Template of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(),
      TemplateTrigger.of(row.findCell(1).stringValue()),
      row.findCell(2).<String>listValue().stream()
        .map(TemplateAction::of).toList(), row.findCell(3).listValue(),
      row.findCell(4).listValue(), row.findCell(5).listValue(),
      TemplateAccessType.valueOf(row.findCell(6).stringValue()));
  }

  public static Template create(
    UUID id, TemplateTrigger trigger, List<TemplateAction> actions,
    List<String> name, List<String> description, TemplateAccessType accessType
  ) {
    var modules = Lists.newArrayList(trigger.module());
    modules.addAll(actions.stream().map(TemplateAction::module).toList());
    return create(id, trigger, actions, modules, name, description, accessType);
  }

  private final UUID id;
  private final TemplateTrigger trigger;
  private final List<TemplateAction> actions;
  private final List<String> modules;
  private final List<String> name;
  private final List<String> description;
  private final TemplateAccessType accessType;

  public String translateName(String language) {
    return switch(language.toLowerCase()) {
      case "en" -> name.size() > 0 ? name.get(0) : "";
      case "de" -> name.size() > 1 ? name.get(1) : "";
      default -> "LANGUAGE NOT FOUND";
    };
  }

  public String translateDescription(String language) {
    return switch(language.toLowerCase()) {
      case "en" -> description.size() > 0 ? description.get(0) : "";
      case "de" -> description.size() > 1 ? description.get(1) : "";
      default -> "LANGUAGE NOT FOUND";
    };
  }
}
