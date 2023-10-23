package net.taskwolf.core.template;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.json.JSONObject;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class TemplateAction {
  public static TemplateAction of(String content) {
    var json = new JSONObject(content);
    return create(json.getString("module"), json.getString("type"),
      json.getString("content"));
  }

  private final String module;
  private final String type;
  private final String content;

  public String encode() {
    var json = new JSONObject();
    json.put("module", module);
    json.put("type", type);
    json.put("content", content);
    return json.toString();
  }
}
