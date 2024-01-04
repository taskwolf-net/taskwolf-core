package net.taskwolf.core.locale;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.configuration.Configuration;
import org.json.JSONObject;

import java.util.Map;

@Getter
@Accessors(fluent = true)
public final class Locale extends Configuration {
  private static final String LOCALE_PATH = "/locale/%s/%s.json";

  public static Locale createAndLoad(String language) throws Exception {
    var configuration = new Locale(String.format(LOCALE_PATH, language, language));
    configuration.load();
    return configuration;
  }

  private final Map<String, String> locale = Maps.newHashMap();

  private Locale(String path) {
    super(path);
  }

  @Override
  protected void deserialize(JSONObject json) {
    for (var key : json.keySet()) {
      locale.put(key, json.getString(key));
    }
  }

  public String findText(String key) {
    if (!locale.containsKey(key)) {
      return key;
    }
    return locale.get(key);
  }
}