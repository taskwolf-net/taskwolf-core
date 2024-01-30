package net.taskwolf.core.distribution.server.packet.user;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.packet.PacketBuffer;
import net.taskwolf.core.distribution.server.packet.PacketIncoming;

import java.util.List;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingUsersReorganize extends PacketIncoming {
  private String module;
  private List<UUID> users;

  public PacketIncomingUsersReorganize() {
    super(0x07);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    module = buffer.readString();
    var usersLength = buffer.readVarInt();
    users = Lists.newArrayList();
    for (var i = 0; i < usersLength; i++) {
      users.add(buffer.readUUID());
    }
  }
}