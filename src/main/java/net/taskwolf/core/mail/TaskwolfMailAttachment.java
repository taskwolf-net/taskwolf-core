package net.taskwolf.core.mail;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.io.File;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public class TaskwolfMailAttachment {
  private final String name;
  private final File file;
}
