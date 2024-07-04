package net.taskwolf.core.stripe;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.configuration.Configuration;
import org.json.JSONObject;

@Getter
@Accessors(fluent = true)
public final class StripeConfiguration extends Configuration {
  private static final String CONFIGURATION_PATH = "/configurations/stripe/stripe.json";

  public static StripeConfiguration createAndLoad() throws Exception {
    var configuration = new StripeConfiguration(CONFIGURATION_PATH);
    configuration.load();
    return configuration;
  }

  private String webhookSecret;

  private StripeConfiguration(String path) {
    super(path);
  }

  @Override
  protected void deserialize(JSONObject json) {
    webhookSecret = json.getString("webhookSecret");
  }
}