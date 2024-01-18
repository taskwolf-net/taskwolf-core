package net.taskwolf.core.workflow.notification;

import lombok.RequiredArgsConstructor;
import net.taskwolf.core.mail.TaskwolfMail;
import net.taskwolf.core.notification.Notification;

@RequiredArgsConstructor(staticName = "create")
public final class WorkflowFailureNotification implements Notification {
  private final TaskwolfMail notificationMail;
  private final String target;
  private final String failureMessage;

  private static final String NOTIFICATION_TITLE = "Workflow failed";
  private static final String NOTIFICATION_BODY = "Hey,\n" +
    "\n" +
    "Unfortunately we have some bad news. \n" +
    "\n" +
    "One of your Taskwolf workflows has failed and requires your attention.\n" +
    "\n" +
    "The reason for the error:\n" +
    "\n" +
    "%s" +
    "\n" +
    "Please log in to your Taskwolf account to fix the error.\n" +
    "\n" +
    "If you have any questions about your problem, you can contact support@taskwolf.net at any time or create a ticket.\n" +
    "\n" +
    "We can then help you to rectify the error";

  public void send() {
    notificationMail.send(target, NOTIFICATION_TITLE,
      String.format(NOTIFICATION_BODY, failureMessage));
  }
}
