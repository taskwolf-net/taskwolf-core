package net.taskwolf.core.mail;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.internet.MimeMultipart;
import java.util.Date;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class MailMessage {
  public static MailMessage of(Message message) {
    try {
      var content = (MimeMultipart) message.getContent();
      var body = new StringBuilder();
      for (var i = 0; i < content.getCount(); i++) {
        var bodyPart = content.getBodyPart(i);
        if (bodyPart.isMimeType("text/plain")) {
          body.append(bodyPart.getContent());
        }
      }
      return create(message.getSubject(), body.toString(), message.getFrom()[0],
        message.getReceivedDate());
    } catch (Exception exception) {
      exception.printStackTrace();
      return null;
    }
  }

  private final String title;
  private final String body;
  private final Address sender;
  private final Date receiveDate;
}
