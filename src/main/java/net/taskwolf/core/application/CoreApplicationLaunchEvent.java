package net.taskwolf.core.application;

import net.taskwolf.core.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class CoreApplicationLaunchEvent extends Event {
}