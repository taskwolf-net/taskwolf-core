package net.taskwolf.core.user;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.configuration.Configuration;
import org.json.JSONObject;

@Getter
@Accessors(fluent = true)
public final class ProfilePictureConfiguration extends Configuration {
  private static final String CONFIGURATION_PATH = "/configurations/user/profile-picture.json";

  public static ProfilePictureConfiguration createAndLoad() throws Exception {
    var configuration = new ProfilePictureConfiguration(CONFIGURATION_PATH);
    configuration.load();
    return configuration;
  }

  private String defaultProfilePicture;

  private ProfilePictureConfiguration(String path) {
    super(path);
  }

  @Override
  protected void deserialize(JSONObject json) {
    defaultProfilePicture = json.getString("defaultProfilePicture");
  }
}