package net.taskwolf.core.stripe;

import net.taskwolf.core.bundle.BundleClass;
import net.taskwolf.core.bundle.BundleRuntime;
import net.taskwolf.core.configuration.Configuration;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.bundle.BundlePreset;
import net.taskwolf.core.bundle.BundleType;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
  private String checkoutWebhookSecret;
  private String paymentWebhookSecret;
  private Map<String, String> priceIds;

  private StripeConfiguration(String path) {
    super(path);
  }

  @Override
  protected void deserialize(JSONObject json) {
    apiKey = json.getString("apiKey");
    checkoutWebhookSecret = json.getString("checkoutWebhookSecret");
    paymentWebhookSecret = json.getString("paymentWebhookSecret");
    priceIds = Maps.newHashMap();
    registerPriceId(json, BundleType.INDIVIDUAL, BundleClass.BEGINNER, BundleRuntime.MONTHLY);
    registerPriceId(json, BundleType.INDIVIDUAL, BundleClass.ADVANCED, BundleRuntime.MONTHLY);
    registerPriceId(json, BundleType.INDIVIDUAL, BundleClass.EXPERT, BundleRuntime.MONTHLY);
    registerPriceId(json, BundleType.INDIVIDUAL, BundleClass.BEGINNER, BundleRuntime.YEARLY);
    registerPriceId(json, BundleType.INDIVIDUAL, BundleClass.ADVANCED, BundleRuntime.YEARLY);
    registerPriceId(json, BundleType.INDIVIDUAL, BundleClass.EXPERT, BundleRuntime.YEARLY);
    registerPriceId(json, BundleType.TEAM, BundleClass.BEGINNER, BundleRuntime.MONTHLY);
    registerPriceId(json, BundleType.TEAM, BundleClass.ADVANCED, BundleRuntime.MONTHLY);
    registerPriceId(json, BundleType.TEAM, BundleClass.EXPERT, BundleRuntime.MONTHLY);
    registerPriceId(json, BundleType.TEAM, BundleClass.BEGINNER, BundleRuntime.YEARLY);
    registerPriceId(json, BundleType.TEAM, BundleClass.ADVANCED, BundleRuntime.YEARLY);
    registerPriceId(json, BundleType.TEAM, BundleClass.EXPERT, BundleRuntime.YEARLY);
  }

  private void registerPriceId(
    JSONObject json, BundleType bundleType, BundleClass bundleClass,
    BundleRuntime bundleRuntime
  ) {
    var typeString = switch(bundleType) {
      case INDIVIDUAL -> "Individual";
      case TEAM -> "Team";
      case TRIAL, ENTERPRISE -> "";
    };
    var classString = switch(bundleClass) {
      case BEGINNER -> "Beginner";
      case ADVANCED -> "Advanced";
      case EXPERT -> "Expert";
      case NONE -> "";
    };
    var runtimeString = switch(bundleRuntime) {
      case MONTHLY -> "Monthly";
      case YEARLY -> "Yearly";
      case INFINITE, UNBOUND -> "";
    };
    priceIds.put(bundleType.toString() + "-" + bundleClass.toString() + "-" +
        bundleRuntime.toString(),
      json.getString("price" + typeString + classString + runtimeString + "Id"));
  }

  public String findPriceId(
    BundleType bundleType, BundleClass bundleClass, BundleRuntime bundleRuntime
  ) {
    return priceIds.get(bundleType.toString() + "-" + bundleClass.toString() +
      "-" + bundleRuntime.toString());
  }

  public boolean priceIdExists(String priceId) {
    return priceIds.containsValue(priceId);
  }

  public List<String> findPriceIdsOfType(BundleType bundleType) {
    var priceIds = Lists.<String>newArrayList();
    priceIds.add(findPriceId(bundleType, BundleClass.BEGINNER,
      BundleRuntime.MONTHLY));
    priceIds.add(findPriceId(bundleType, BundleClass.ADVANCED,
      BundleRuntime.MONTHLY));
    priceIds.add(findPriceId(bundleType, BundleClass.EXPERT,
      BundleRuntime.MONTHLY));
    priceIds.add(findPriceId(bundleType, BundleClass.BEGINNER,
      BundleRuntime.YEARLY));
    priceIds.add(findPriceId(bundleType, BundleClass.ADVANCED,
      BundleRuntime.YEARLY));
    priceIds.add(findPriceId(bundleType, BundleClass.EXPERT,
      BundleRuntime.YEARLY));
    return priceIds;
  }

  public List<String> findPriceIdsOfTypeAndClass(
    BundleType bundleType, BundleClass bundleClass
  ) {
    var priceIds = Lists.<String>newArrayList();
    priceIds.add(findPriceId(bundleType, bundleClass,
      BundleRuntime.MONTHLY));
    priceIds.add(findPriceId(bundleType, bundleClass,
      BundleRuntime.YEARLY));
    return priceIds;
  }

  public List<String> findPriceIdsOfRuntime(BundleRuntime bundleRuntime) {
    var priceIds = Lists.<String>newArrayList();
    priceIds.add(findPriceId(BundleType.INDIVIDUAL, BundleClass.BEGINNER,
      bundleRuntime));
    priceIds.add(findPriceId(BundleType.INDIVIDUAL, BundleClass.ADVANCED,
      bundleRuntime));
    priceIds.add(findPriceId(BundleType.INDIVIDUAL, BundleClass.EXPERT,
      bundleRuntime));
    priceIds.add(findPriceId(BundleType.TEAM, BundleClass.BEGINNER,
      bundleRuntime));
    priceIds.add(findPriceId(BundleType.TEAM, BundleClass.ADVANCED,
      bundleRuntime));
    priceIds.add(findPriceId(BundleType.TEAM, BundleClass.EXPERT,
      bundleRuntime));
    return priceIds;
  }

  public Optional<BundlePreset> findBundlePreset(String priceId) throws Exception {
    if (findPriceIdsOfTypeAndClass(BundleType.INDIVIDUAL, BundleClass.BEGINNER).contains(priceId)) {
      return Optional.of(BundlePreset.createAndLoad(BundleType.INDIVIDUAL, BundleClass.BEGINNER));
    } else if (findPriceIdsOfTypeAndClass(BundleType.INDIVIDUAL, BundleClass.ADVANCED).contains(priceId)) {
      return Optional.of(BundlePreset.createAndLoad(BundleType.INDIVIDUAL, BundleClass.ADVANCED));
    } else if (findPriceIdsOfTypeAndClass(BundleType.INDIVIDUAL, BundleClass.EXPERT).contains(priceId)) {
      return Optional.of(BundlePreset.createAndLoad(BundleType.INDIVIDUAL, BundleClass.EXPERT));
    } else if (findPriceIdsOfTypeAndClass(BundleType.TEAM, BundleClass.BEGINNER).contains(priceId)) {
      return Optional.of(BundlePreset.createAndLoad(BundleType.TEAM, BundleClass.BEGINNER));
    } else if (findPriceIdsOfTypeAndClass(BundleType.TEAM, BundleClass.ADVANCED).contains(priceId)) {
      return Optional.of(BundlePreset.createAndLoad(BundleType.TEAM, BundleClass.ADVANCED));
    } else if (findPriceIdsOfTypeAndClass(BundleType.TEAM, BundleClass.EXPERT).contains(priceId)) {
      return Optional.of(BundlePreset.createAndLoad(BundleType.TEAM, BundleClass.EXPERT));
    }
    return Optional.empty();
  }
}