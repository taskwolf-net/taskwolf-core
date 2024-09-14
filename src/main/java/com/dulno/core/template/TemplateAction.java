package com.dulno.core.template;

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
    return create(json.getInt("actionIndex"), json.getString("module"),
      json.getString("type"));
  }

  private final int actionIndex;
  private final String module;
  private final String type;

  public String encode() {
    var json = new JSONObject();
    json.put("actionIndex", actionIndex);
    json.put("module", module);
    json.put("type", type);
    return json.toString();
  }
}
