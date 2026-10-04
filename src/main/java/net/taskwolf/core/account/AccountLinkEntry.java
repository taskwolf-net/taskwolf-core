package net.taskwolf.core.account;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class AccountLinkEntry {
  private final String identifier;
  private final String name;
}