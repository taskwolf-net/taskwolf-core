package net.taskwolf.core.distribution.client.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.client.packet.PacketEncoder;

@RequiredArgsConstructor(staticName = "create")
public final class ClientChannelEquipment extends ChannelInitializer<Channel> {
  @Override
  protected void initChannel(Channel channel) {
    channel.pipeline().addLast("channel-encoder", PacketEncoder.create());
    channel.pipeline().addLast("channel-inbox", ClientChannelInbox.create());
  }
}