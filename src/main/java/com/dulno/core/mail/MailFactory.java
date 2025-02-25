package com.dulno.core.mail;

import com.dulno.core.error.ErrorRepository;
import com.dulno.core.locale.Translation;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class MailFactory {
  private final OutgoingMailDatabaseTable outgoingMailDatabaseTable;
  private final ErrorRepository errorRepository;
  private final MailTemplate mailTemplate;
  private final Translation translation;

  public Mail create(
    String mail, String smtpMailHost, int smtpMailPort, String imapMailHost,
    int imapMailPort, String mailUser, String mailPassword
  ) {
    return Mail.create(outgoingMailDatabaseTable, errorRepository, mailTemplate,
      translation, mail, smtpMailHost, smtpMailPort, imapMailHost, imapMailPort,
      mailUser, mailPassword);
  }
}
