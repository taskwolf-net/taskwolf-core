package net.taskwolf.core.mail;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskwolfMail {
  public static TaskwolfMail create(
    String mail, String smtpMailHost, int smtpMailPort, String imapMailHost,
    int imapMailPort, String mailUser, String mailPassword
  ) {
    return new TaskwolfMail(mail, smtpMailHost, smtpMailPort, imapMailHost,
      imapMailPort, mailUser, mailPassword);
  }

  private final String mail;
  private final String smtpMailHost;
  private final int smtpMailPort;
  private final String imapMailHost;
  private final int imapMailPort;
  private final String mailUser;
  private final String mailPassword;

  public CompletableFuture<List<TaskwolfMailMessage>> inbox() {
    var futureResponse = new CompletableFuture<List<TaskwolfMailMessage>>();
    new Thread(() -> futureResponse.complete(readInbox(false))).start();
    return futureResponse;
  }

  public CompletableFuture<List<TaskwolfMailMessage>> inboxAndFlush() {
    var futureResponse = new CompletableFuture<List<TaskwolfMailMessage>>();
    new Thread(() -> futureResponse.complete(readInbox(true))).start();
    return futureResponse;
  }

  private List<TaskwolfMailMessage> readInbox(boolean delete) {
    try {
      var session = createSession("imap", imapMailHost, imapMailPort);
      var store = session.getStore("imap");
      store.connect(imapMailHost, mailUser, mailPassword);
      var folder = store.getFolder("INBOX");
      folder.open(Folder.READ_WRITE);
      if (delete) {
        deleteMessages(folder.getMessages());
      }
      var messages = Arrays.stream(folder.getMessages())
        .map(TaskwolfMailMessage::of).toList();
      folder.close(true);
      store.close();
      return messages;
    } catch (Exception exception) {
      exception.printStackTrace();
      return null;
    }
  }

  private void deleteMessages(Message[] messages) throws Exception {
    for (var i = 0; i < messages.length; i++) {
      messages[i].setFlag(Flags.Flag.DELETED, true);
    }
  }

  public void send(String target, String title, String body) {
    send(new Address[] {createAddress(target)}, title, body);
  }

  public void send(Address[] addresses, String title, String body) {
    new Thread(() -> sendEmail(addresses, title, body)).start();
  }

  private Address createAddress(String email) {
    try {
      return new InternetAddress(email);
    } catch (Exception exception) {
      exception.printStackTrace();
      return null;
    }
  }

  private void sendEmail(Address[] addresses, String title, String body) {
    try {
      var session = createSession("smtp", smtpMailHost, smtpMailPort);
      var message = createMessage(session, addresses, title, body);
      var transport = session.getTransport("smtp");
      transport.connect(smtpMailHost, mailUser, mailPassword);
      transport.sendMessage(message, message.getAllRecipients());
      transport.close();
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  private Session createSession(String protocol, String host, int port) {
    var properties = System.getProperties();
    properties.put("mail." + protocol + ".host", host);
    properties.put("mail." + protocol + ".port", port);
    properties.put("mail." + protocol + ".starttls.enable", "true");
    properties.put("mail." + protocol + ".socketFactory.class",
      "javax.net.ssl.SSLSocketFactory");
    var session = Session.getDefaultInstance(properties, null);
    session.setDebug(false);
    return session;
  }

  private Message createMessage(
    Session session, Address[] addresses, String title, String body
  ) throws Exception {
    var message = new MimeMessage(session);
    message.setFrom(new InternetAddress(mail, "Taskwolf"));
    message.setRecipients(Message.RecipientType.TO, addresses);
    message.setSubject(title);
    message.setText(body);
    message.setSentDate(new Date());
    return message;
  }
}
