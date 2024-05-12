package net.taskwolf.core.distribution.packet;

import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RequiredArgsConstructor(staticName = "create")
public final class PacketBuffer {
  private final ByteBuf buffer;

  /**
   * Writes a var int to the buffer
   * @param value The integer value
   */
  public void writeVarInt(int value) {
    int part;
    do {
      part = value & 0x7F;
      value >>>= 7;
      if (value != 0) {
        part |= 0x80;
      }
      buffer.writeByte(part);
    } while (value != 0);
  }

  /**
   * Writes a string to the buffer
   * @param value The string value
   */
  public void writeString(String value) {
    var bytes = value.getBytes(StandardCharsets.UTF_8);
    writeVarInt(bytes.length);
    buffer.writeBytes(bytes);
  }

  /**
   * Writes a uuid to the buffer
   * @param uuid The uuid value
   */
  public void writeUUID(UUID uuid) {
    buffer.writeLong(uuid.getMostSignificantBits());
    buffer.writeLong(uuid.getLeastSignificantBits());
  }

  /**
   * Reads a var int from the buffer
   * @return The integer value
   */
  public int readVarInt() {
    var out = 0;
    var bytes = 0;
    byte in;
    do {
      in = buffer.readByte();
      out |= (in & 0x7F) << (bytes++ * 7);
    } while ((in & 0x80) == 0x80);
    return out;
  }

  /**
   * Reads a string from the buffer
   * @return The string value
   */
  public String readString() {
    var length = readVarInt();
    var bytes = new byte[length];
    buffer.readBytes(bytes);
    return new String(bytes);
  }

  /**
   * Reads a uuid from the buffer
   * @return The uuid value
   */
  public UUID readUUID() {
    var mostSignificantBits = buffer.readLong();
    var leastSignificantBits = buffer.readLong();
    return new UUID(mostSignificantBits, leastSignificantBits);
  }

  /**
   * Is used to get the raw {@link ByteBuf}
   * @return The raw buffer
   */
  public ByteBuf raw() {
    return buffer;
  }

  /**
   * Is used to get the bytes from the buffer
   * @return The bytes of the buffer
   */
  public byte[] bytes() {
    var temp = new byte[buffer.readableBytes()];
    buffer.readBytes(temp);
    return temp;
  }
}
