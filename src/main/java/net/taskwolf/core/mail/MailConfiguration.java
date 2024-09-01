package net.taskwolf.core.mail;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.configuration.Configuration;
import org.json.JSONObject;

@Getter
@Accessors(fluent = true)
public final class MailConfiguration extends Configuration {
  private static final String CONFIGURATION_PATH = "/configurations/mail/%s.json";

  public static MailConfiguration createAndLoad(String mail) throws Exception {
    var configuration = new MailConfiguration(
      String.format(CONFIGURATION_PATH, mail));
    configuration.load();
    return configuration;
  }

  private String mail;
  private String smtpMailHost;
  private int smtpMailPort;
  private String imapMailHost;
  private int imapMailPort;
  private String mailUser;
  private String mailPassword;

  private MailConfiguration(String path) {
    super(path);
  }

  @Override
  protected void deserialize(JSONObject json) {
    mail = json.getString("mail");
    smtpMailHost = json.getString("smtpMailHost");
    smtpMailPort = json.getInt("smtpMailPort");
    imapMailHost = json.getString("imapMailHost");
    imapMailPort = json.getInt("imapMailPort");
    mailUser = json.getString("mailUser");
    mailPassword = json.getString("mailPassword");
  }
}