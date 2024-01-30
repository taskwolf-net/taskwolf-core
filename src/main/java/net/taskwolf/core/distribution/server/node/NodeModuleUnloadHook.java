package net.taskwolf.core.distribution.server.node;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.core.event.node.NodeModuleUnloadEvent;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class NodeModuleUnloadHook implements Hook {
  @EventHook
  private void nodeModuleUnload(NodeModuleUnloadEvent event) {
    event.client().condition().removeModule(event.module());
  }
}

