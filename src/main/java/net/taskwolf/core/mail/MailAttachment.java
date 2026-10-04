package net.taskwolf.core.mail;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.io.File;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public class MailAttachment {
  private final String name;
  private final File file;
}
