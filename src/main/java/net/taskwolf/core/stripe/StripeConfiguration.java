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

  private String apiKey;
  private String webhookSecret;
  private String professionalBeginnerMonthlyProductId;
  private String professionalAdvancedMonthlyProductId;
  private String professionalExpertMonthlyProductId;
  private String professionalBeginnerYearlyProductId;
  private String professionalAdvancedYearlyProductId;
  private String professionalExpertYearlyProductId;
  private String teamBeginnerMonthlyProductId;
  private String teamAdvancedMonthlyProductId;
  private String teamExpertMonthlyProductId;
  private String teamBeginnerYearlyProductId;
  private String teamAdvancedYearlyProductId;
  private String teamExpertYearlyProductId;

  private StripeConfiguration(String path) {
    super(path);
  }

  @Override
  protected void deserialize(JSONObject json) {
    apiKey = json.getString("apiKey");
    webhookSecret = json.getString("webhookSecret");
    professionalBeginnerMonthlyProductId = json.getString("professionalBeginnerMonthlyProductId");
    professionalAdvancedMonthlyProductId = json.getString("professionalAdvancedMonthlyProductId");
    professionalExpertMonthlyProductId = json.getString("professionalExpertMonthlyProductId");
    professionalBeginnerYearlyProductId = json.getString("professionalBeginnerYearlyProductId");
    professionalAdvancedYearlyProductId = json.getString("professionalAdvancedYearlyProductId");
    professionalExpertYearlyProductId = json.getString("professionalExpertYearlyProductId");
    teamBeginnerMonthlyProductId = json.getString("teamBeginnerMonthlyProductId");
    teamAdvancedMonthlyProductId = json.getString("teamAdvancedMonthlyProductId");
    teamExpertMonthlyProductId = json.getString("teamExpertMonthlyProductId");
    teamBeginnerYearlyProductId = json.getString("teamBeginnerYearlyProductId");
    teamAdvancedYearlyProductId = json.getString("teamAdvancedYearlyProductId");
    teamExpertYearlyProductId = json.getString("teamExpertYearlyProductId");
  }
}