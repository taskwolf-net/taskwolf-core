package net.taskwolf.core.mail;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class TaskwolfMail {
  private final String mailHost;
  private final String mail;
  private final String mailPassword;
  private final String target;

  public void send() {
    new Thread(() -> sendEmail(target, emailTitle(), emailBody())).start();
  }

  protected abstract String emailTitle();

  protected abstract String emailBody();

  private void sendEmail(String target, String title, String body) {
    try {
      var session = createEmailSession();
      var message = createMessage(session, target, title, body);
      var transport = session.getTransport("smtp");
      transport.connect(mailHost, mail, mailPassword);
      transport.sendMessage(message, message.getAllRecipients());
      transport.close();
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  private Session createEmailSession() {
    var props = System.getProperties();
    props.put("mail.smtp.host", mailHost);
    props.put("mail.smtp.port", "465");
    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
    var session = Session.getDefaultInstance(props, null);
    session.setDebug(false);
    return session;
  }

  private Message createMessage(Session session, String target, String title, String body) throws Exception {
    var message = new MimeMessage(session);
    message.setFrom(new InternetAddress(mail, "Taskwolf"));
    var address = new InternetAddress[]{new InternetAddress(target)};
    message.setRecipients(Message.RecipientType.TO, address);
    message.setSubject(title);
    message.setText(body);
    message.setSentDate(new Date());
    return message;
  }
}
