package com.dulno.core.notification;

import com.dulno.core.mail.Mail;
import com.dulno.core.mail.MailConfiguration;
import com.dulno.core.mail.MailFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;

@RequiredArgsConstructor(staticName = "create")
public final class NotificationInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  NotificationDatabaseTable provideNotificationDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var notificationDatabaseTable = NotificationDatabaseTable.create(connection,
      keyspace);
    notificationDatabaseTable.createIfNotExists();
    return notificationDatabaseTable;
  }

  @Provides
  @Singleton
  @Named("notificationMail")
  Mail provideNotificationMail(MailFactory mailFactory) throws Exception {
    var mailConfiguration = MailConfiguration.createAndLoad("notification");
    return mailFactory.create(mailConfiguration.mail(),
      mailConfiguration.smtpMailHost(), mailConfiguration.smtpMailPort(),
      mailConfiguration.imapMailHost(), mailConfiguration.imapMailPort(),
      mailConfiguration.mailUser(), mailConfiguration.mailPassword());
  }
}
