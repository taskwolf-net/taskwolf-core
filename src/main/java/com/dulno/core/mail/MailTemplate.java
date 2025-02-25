package com.dulno.core.mail;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.Charset;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MailTemplate {
  private static final String CONFIGURATION_PATH = "/configurations/mail/template.html";

  public static MailTemplate createAndLoad() throws Exception {
    var configuration = new MailTemplate(CONFIGURATION_PATH);
    configuration.load();
    return configuration;
  }

  private final String path;
  private String mailTemplate;

  public void load() throws Exception  {
    mailTemplate = FileUtils.readFileToString(
      new File(absolutePath()), Charset.defaultCharset());
  }

  private String absolutePath() {
    return System.getProperty("user.dir") + path;
  }
}
