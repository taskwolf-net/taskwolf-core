package net.taskwolf.core.notification;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.mail.Mail;
import net.taskwolf.core.mail.MailConfiguration;
import net.taskwolf.core.mail.MailFactory;

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
