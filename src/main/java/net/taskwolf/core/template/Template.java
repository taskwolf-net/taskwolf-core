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
      row.findCell(4).stringValue(), row.findCell(5).stringValue());
  }

  public static Template create(
    UUID id, TemplateTrigger trigger, List<TemplateAction> actions,
    String name, String description
  ) {
    var modules = Lists.newArrayList(trigger.module());
    modules.addAll(actions.stream().map(TemplateAction::module).toList());
    return create(id, trigger, actions, modules, name, description);
  }

  private final UUID id;
  private final TemplateTrigger trigger;
  private final List<TemplateAction> actions;
  private final List<String> modules;
  private final String name;
  private final String description;
}
