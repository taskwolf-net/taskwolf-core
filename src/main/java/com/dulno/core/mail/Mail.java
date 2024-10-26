package com.dulno.core.mail;

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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
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

  public CompletableFuture<String> send(String target, String title, String body) {
    return send(target, title, body, Lists.newArrayList());
  }

  public CompletableFuture<String> send(
    String target, String title, String body, String dataType
  ) {
    return send(target, title, body, dataType, Lists.newArrayList());
  }

  public CompletableFuture<String> send(
    String target, String title, String body,
    List<MailAttachment> attachments
  ) {
    return send(target, title, body, "", attachments);
  }

  public CompletableFuture<String> send(
    String target, String title, String body, String dataType,
    List<MailAttachment> attachments
  ) {
    var futureResponse = new CompletableFuture<String>();
    new Thread(() -> sendEmail(target, title, body, dataType,
      attachments, futureResponse)).start();
    return futureResponse;
  }

  private void sendEmail(
    String target, String title, String body, String dataType,
    List<MailAttachment> attachments,
    CompletableFuture<String> futureResponse
  ) {
    try {
      var session = createSession("smtp", smtpMailHost, smtpMailPort);
      var message = createMessage(session, new Address[] {createAddress(target)},
        title, body, dataType, attachments);
      var transport = session.getTransport("smtp");
      transport.connect(smtpMailHost, mailUser, mailPassword);
      transport.sendMessage(message, message.getAllRecipients());
      transport.close();
      var messageId = message.getHeader("Message-ID")[0];
      outgoingMailDatabaseTable.generateAvailableOutgoingMailId()
        .thenAccept(id -> outgoingMailDatabaseTable.insertOutgoingMail(id, target,
          mail, System.currentTimeMillis(), title, serializeMessage(message))
          .thenAccept(value -> futureResponse.complete(messageId)));
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

  private MimeMessage createMessage(
    Session session, Address[] addresses, String title, String body,
    String dataType, List<MailAttachment> attachments
  ) throws Exception {
    var message = new MimeMessage(session);
    message.setFrom(new InternetAddress(mail, "Dulno"));
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

  private byte[] serializeMessage(MimeMessage message) {
    try {
      var byteArrayOutputStream = new ByteArrayOutputStream();
      message.writeTo(byteArrayOutputStream);
      return byteArrayOutputStream.toByteArray();
    } catch (Exception exception) {
      exception.printStackTrace();
      return null;
    }
  }

  public void resendEmail(OutgoingMail outgoingMail) {
    try {
      var session = createSession("smtp", smtpMailHost, smtpMailPort);
      var message = deserializeMessage(session, outgoingMail.content());
      message.setSentDate(new Date());
      var transport = session.getTransport("smtp");
      transport.connect(smtpMailHost, mailUser, mailPassword);
      transport.sendMessage(message, message.getAllRecipients());
      transport.close();
      outgoingMailDatabaseTable.generateAvailableOutgoingMailId()
        .thenAccept(id -> outgoingMailDatabaseTable.insertOutgoingMail(id,
          outgoingMail.receiver(), mail, System.currentTimeMillis(),
          outgoingMail.title(), serializeMessage(message)));
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  private MimeMessage deserializeMessage(Session session, byte[] content) {
    try {
      var byteArrayInputStream = new ByteArrayInputStream(content);
      return new MimeMessage(session, byteArrayInputStream);
    } catch (Exception exception) {
      exception.printStackTrace();
      return null;
    }
  }
}
