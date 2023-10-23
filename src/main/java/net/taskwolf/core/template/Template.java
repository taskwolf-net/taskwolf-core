package net.taskwolf.core.template;

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
        .map(TemplateAction::of).toList(), row.findCell(3).stringValue(),
      row.findCell(4).stringValue());
  }

  private final UUID id;
  private final TemplateTrigger trigger;
  private final List<TemplateAction> actions;
  private final String name;
  private final String description;
}
