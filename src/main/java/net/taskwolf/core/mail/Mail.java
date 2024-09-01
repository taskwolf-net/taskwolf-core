package net.taskwolf.core.mail;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class Mail {
  public static Mail create(
    OutgoingMailDatabaseTable outgoingMailDatabaseTable, String mail,
    String smtpMailHost, int smtpMailPort, String imapMailHost, int imapMailPort,
    String mailUser, String mailPassword
  ) {
    return new Mail(outgoingMailDatabaseTable, mail, smtpMailHost,
      smtpMailPort, imapMailHost, imapMailPort, mailUser, mailPassword);
  }

  private final OutgoingMailDatabaseTable outgoingMailDatabaseTable;
  private final String mail;
  private final String smtpMailHost;
  private final int smtpMailPort;
  private final String imapMailHost;
  private final int imapMailPort;
  private final String mailUser;
  private final String mailPassword;

  public CompletableFuture<List<MailMessage>> inbox() {
    var futureResponse = new CompletableFuture<List<MailMessage>>();
    new Thread(() -> futureResponse.complete(readInbox(false))).start();
    return futureResponse;
  }

  public CompletableFuture<List<MailMessage>> inboxAndFlush() {
    var futureResponse = new CompletableFuture<List<MailMessage>>();
    new Thread(() -> futureResponse.complete(readInbox(true))).start();
    return futureResponse;
  }

  private List<MailMessage> readInbox(boolean delete) {
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
        .map(MailMessage::of).toList();
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

  public CompletableFuture<Void> send(String target, String title, String body) {
    return send(target, title, body, Lists.newArrayList());
  }

  public CompletableFuture<Void> send(
    String target, String title, String body, String dataType
  ) {
    return send(target, title, body, dataType, Lists.newArrayList());
  }

  public CompletableFuture<Void> send(
    String target, String title, String body,
    List<MailAttachment> attachments
  ) {
    return send(target, title, body, "", attachments);
  }

  public CompletableFuture<Void> send(
    String target, String title, String body, String dataType,
    List<MailAttachment> attachments
  ) {
    var futureResponse = new CompletableFuture<Void>();
    new Thread(() -> sendEmail(target, title, body, dataType,
      attachments, futureResponse)).start();
    return futureResponse;
  }

  private void sendEmail(
    String target, String title, String body, String dataType,
    List<MailAttachment> attachments,
    CompletableFuture<Void> futureResponse
  ) {
    try {
      var session = createSession("smtp", smtpMailHost, smtpMailPort);
      var message = createMessage(session, new Address[] {createAddress(target)},
        title, body, dataType, attachments);
      var transport = session.getTransport("smtp");
      transport.connect(smtpMailHost, mailUser, mailPassword);
      transport.sendMessage(message, message.getAllRecipients());
      transport.close();
      var content = message.getContent();
      var contentType = message.getContentType();
      outgoingMailDatabaseTable.generateAvailableOutgoingMailId()
        .thenAccept(id -> outgoingMailDatabaseTable.insertOutgoingMail(id, target,
          mail, System.currentTimeMillis(), title, serializeContent(content),
          contentType));
      futureResponse.complete(null);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  private InternetAddress createAddress(String email) {
    try {
      return new InternetAddress(email);
    } catch (Exception exception) {
      exception.printStackTrace();
      return null;
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
    Session session, Address[] addresses, String title, String body,
    String dataType, List<MailAttachment> attachments
  ) throws Exception {
    var message = new MimeMessage(session);
    message.setFrom(new InternetAddress(mail, "Taskwolf"));
    message.setRecipients(Message.RecipientType.TO, addresses);
    message.setSentDate(new Date());
    message.setSubject(title);
    if (attachments.isEmpty()) {
      if (dataType.isEmpty()) {
        message.setText(body);
      } else {
        message.setContent(body, dataType);
      }
    } else {
      message.setContent(createMultipartBody(body, attachments));
    }
    return message;
  }

  private MimeMultipart createMultipartBody(
    String body, List<MailAttachment> attachments
  ) throws Exception {
    var multipart = new MimeMultipart();
    var textBodyPart = new MimeBodyPart();
    textBodyPart.setText(body);
    multipart.addBodyPart(textBodyPart);
    for (var attachment : attachments) {
      addAttachmentPart(attachment, multipart);
    }
    return multipart;
  }

  private void addAttachmentPart(
    MailAttachment attachment, MimeMultipart multipart
  ) throws Exception {
    var attachmentBodyPart = new MimeBodyPart();
    var source = new FileDataSource(attachment.file().getAbsolutePath());
    attachmentBodyPart.setDataHandler(new DataHandler(source));
    attachmentBodyPart.setFileName(attachment.name());
    multipart.addBodyPart(attachmentBodyPart);
  }

  private String serializeContent(Object content) {
    try {
      var byteArrayOutputStream = new ByteArrayOutputStream();
      var objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
      objectOutputStream.writeObject(content);
      objectOutputStream.flush();
      return new String(Base64.getEncoder().encode(
        byteArrayOutputStream.toByteArray()));
    } catch (Exception exception) {
      exception.printStackTrace();
      return "";
    }
  }

  public void resendEmail(OutgoingMail outgoingMail) {
    try {
      var session = createSession("smtp", smtpMailHost, smtpMailPort);
      var message = createResendMessage(session, outgoingMail);
      var transport = session.getTransport("smtp");
      transport.connect(smtpMailHost, mailUser, mailPassword);
      transport.sendMessage(message, message.getAllRecipients());
      transport.close();
      var content = message.getContent();
      var contentType = message.getContentType();
      outgoingMailDatabaseTable.generateAvailableOutgoingMailId()
        .thenAccept(id -> outgoingMailDatabaseTable.insertOutgoingMail(id,
          outgoingMail.receiver(), mail, System.currentTimeMillis(),
          outgoingMail.title(), serializeContent(content), contentType));
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  private Message createResendMessage(
    Session session, OutgoingMail outgoingMail
  ) throws Exception {
    var message = new MimeMessage(session);
    message.setFrom(new InternetAddress(mail, "Taskwolf"));
    message.setRecipients(Message.RecipientType.TO,
      new Address[] {createAddress(outgoingMail.receiver())});
    message.setSentDate(new Date());
    message.setSubject(outgoingMail.title());
    message.setContent(deserializeContent(outgoingMail.content()),
      outgoingMail.type());
    return message;
  }

  private Object deserializeContent(String content) {
    try {
      var byteArrayInputStream = new ByteArrayInputStream(
        Base64.getDecoder().decode(content));
      var objectInputStream = new ObjectInputStream(byteArrayInputStream);
      return objectInputStream.readObject();
    } catch (Exception exception) {
      exception.printStackTrace();
      return null;
    }
  }
}
