package net.taskwolf.core.template;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.json.JSONObject;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class TemplateTrigger {
  public static TemplateTrigger of(String content) {
    var json = new JSONObject(content);
    return create(json.getString("module"), json.getString("type"));
  }

  private final String module;
  private final String type;

  public String encode() {
    var json = new JSONObject();
    json.put("module", module);
    json.put("type", type);
    return json.toString();
  }
}
