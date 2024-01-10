package net.taskwolf.core.mail;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.configuration.Configuration;
import org.json.JSONObject;

@Getter
@Accessors(fluent = true)
public final class TaskwolfMailConfiguration extends Configuration {
  private static final String CONFIGURATION_PATH = "/configurations/mail/%s.json";

  public static TaskwolfMailConfiguration createAndLoad(String mail) throws Exception {
    var configuration = new TaskwolfMailConfiguration(
      String.format(CONFIGURATION_PATH, mail));
    configuration.load();
    return configuration;
  }

  private String mail;
  private String smtpMailHost;
  private String pop3MailHost;
  private String mailUser;
  private String mailPassword;

  private TaskwolfMailConfiguration(String path) {
    super(path);
  }

  @Override
  protected void deserialize(JSONObject json) {
    mail = json.getString("mail");
    smtpMailHost = json.getString("smtpMailHost");
    pop3MailHost = json.getString("pop3MailHost");
    mailUser = json.getString("mailUser");
    mailPassword = json.getString("mailPassword");
  }
}