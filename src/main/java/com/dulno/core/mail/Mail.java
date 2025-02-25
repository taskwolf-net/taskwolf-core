package com.dulno.core.mail;

import com.dulno.core.error.ErrorRepository;
import com.dulno.core.locale.Translation;
import com.dulno.core.user.User;
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
import java.time.Year;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class Mail {
  public static Mail create(
    OutgoingMailDatabaseTable outgoingMailDatabaseTable,
    ErrorRepository errorRepository, MailTemplate mailTemplate,
    Translation translation, String mail, String smtpMailHost, int smtpMailPort,
    String imapMailHost, int imapMailPort, String mailUser, String mailPassword
  ) {
    return new Mail(outgoingMailDatabaseTable, errorRepository, mailTemplate,
      translation, mail, smtpMailHost, smtpMailPort, imapMailHost,
      imapMailPort, mailUser, mailPassword);
  }

  private final OutgoingMailDatabaseTable outgoingMailDatabaseTable;
  private final ErrorRepository errorRepository;
  private final MailTemplate mailTemplate;
  private final Translation translation;
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
      errorRepository.processError(exception);
      return null;
    }
  }

  private void deleteMessages(Message[] messages) throws Exception {
    for (var i = 0; i < messages.length; i++) {
      messages[i].setFlag(Flags.Flag.DELETED, true);
    }
  }

  public CompletableFuture<String> send(User user, String title, String body) {
    return send(user.email(), user.language(), title, body);
  }

  public CompletableFuture<String> send(
    String target, String language, String title, String body
  ) {
    return send(target, language, title, body, Lists.newArrayList());
  }

  public CompletableFuture<String> send(
    User user, String title, String body, List<MailAttachment> attachments
  ) {
    return send(user.email(), user.language(), title, body, attachments);
  }

  public CompletableFuture<String> send(
    String target, String language, String title, String body,
    List<MailAttachment> attachments
  ) {
    var futureResponse = new CompletableFuture<String>();
    new Thread(() -> sendEmail(target, language, title, body, attachments,
      futureResponse)).start();
    return futureResponse;
  }

  private void sendEmail(
    String target, String language, String title, String body,
    List<MailAttachment> attachments, CompletableFuture<String> futureResponse
  ) {
    try {
      var session = createSession("smtp", smtpMailHost, smtpMailPort);
      var message = createMessage(session, new Address[] {createAddress(target)},
        language, title, body, attachments);
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
      errorRepository.processError(exception);
    }
  }

  private InternetAddress createAddress(String email) {
    try {
      return new InternetAddress(email);
    } catch (Exception exception) {
      errorRepository.processError(exception);
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
    Session session, Address[] addresses, String language, String title,
    String body, List<MailAttachment> attachments
  ) throws Exception {
    var message = new MimeMessage(session);
    message.setFrom(new InternetAddress(mail, "Dulno"));
    message.setRecipients(Message.RecipientType.TO, addresses);
    message.setSentDate(new Date());
    message.setSubject(title);
    var content = buildMessageContent(language, body);
    if (attachments.isEmpty()) {
      message.setContent(content, "text/html; charset=utf-8");
    } else {
      message.setContent(createMultipartBody(content, attachments));
    }
    return message;
  }

  private String buildMessageContent(String language, String body) {
    return mailTemplate.mailTemplate()
      .replaceAll("%YEAR%", String.valueOf(Year.now().getValue()))
      .replaceAll("%RIGHTS%", translation.translate(language, "mail.template.rights"))
      .replaceAll("%IMPRINT%", translation.translate(language, "mail.template.imprint"))
      .replaceAll("%PRIVACY%", translation.translate(language, "mail.template.privacy"))
      .replaceAll("%CONTACT%", translation.translate(language, "mail.template.contact"))
      .replaceAll("%CONTENT%", body.replaceAll("\n", "<br>"));
  }

  private MimeMultipart createMultipartBody(
    String content, List<MailAttachment> attachments
  ) throws Exception {
    var multipart = new MimeMultipart();
    var textBodyPart = new MimeBodyPart();
    textBodyPart.setContent(content, "text/html; charset=utf-8");
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
      errorRepository.processError(exception);
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
      errorRepository.processError(exception);
    }
  }

  private MimeMessage deserializeMessage(Session session, byte[] content) {
    try {
      var byteArrayInputStream = new ByteArrayInputStream(content);
      return new MimeMessage(session, byteArrayInputStream);
    } catch (Exception exception) {
      errorRepository.processError(exception);
      return null;
    }
  }
}
