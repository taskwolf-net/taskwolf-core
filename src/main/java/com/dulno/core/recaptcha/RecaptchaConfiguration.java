package com.dulno.core.recaptcha;

import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.configuration.Configuration;
import org.json.JSONObject;

@Getter
@Accessors(fluent = true)
public final class RecaptchaConfiguration extends Configuration {
  private static final String CONFIGURATION_PATH = "/configurations/recaptcha/recaptcha.json";

  public static RecaptchaConfiguration createAndLoad() throws Exception {
    var configuration = new RecaptchaConfiguration(CONFIGURATION_PATH);
    configuration.load();
    return configuration;
  }

  private String secretKey;

  private RecaptchaConfiguration(String path) {
    super(path);
  }

  @Override
  protected void deserialize(JSONObject json) {
    secretKey = json.getString("secretKey");
  }
}
