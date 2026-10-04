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
    return create(row.findCell(1).uuidValue(),
      TemplateTrigger.of(row.findCell(2).stringValue()),
      row.findCell(3).<String>listValue().stream()
        .map(TemplateAction::of).toList(), row.findCell(4).listValue(),
      row.findCell(5).stringValue(), row.findCell(6).stringValue(),
      row.findCell(7).stringValue(), row.findCell(8).stringValue(),
      TemplateAccessType.valueOf(row.findCell(9).stringValue()));
  }

  public static Template create(
    UUID id, TemplateTrigger trigger, List<TemplateAction> actions,
    String englishName, String englishDescription, String germanName,
    String germanDescription, TemplateAccessType accessType
  ) {
    var modules = Lists.newArrayList(trigger.module());
    modules.addAll(actions.stream().map(TemplateAction::module).toList());
    return create(id, trigger, actions, modules, englishName, englishDescription,
      germanName, germanDescription, accessType);
  }

  private final UUID id;
  private final TemplateTrigger trigger;
  private final List<TemplateAction> actions;
  private final List<String> modules;
  private final String englishName;
  private final String englishDescription;
  private final String germanName;
  private final String germanDescription;
  private final TemplateAccessType accessType;

  public String translateName(String language) {
    return switch(language.toLowerCase()) {
      case "en" -> englishName;
      case "de" -> germanName;
      default -> "LANGUAGE NOT FOUND";
    };
  }

  public String translateDescription(String language) {
    return switch(language.toLowerCase()) {
      case "en" -> englishDescription;
      case "de" -> germanDescription;
      default -> "LANGUAGE NOT FOUND";
    };
  }
}
