package net.taskwolf.core.application;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.event.Event;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class CoreApplicationPostRunEvent extends Event {
}