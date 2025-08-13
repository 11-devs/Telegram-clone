package JSocket2.Protocol;

import JSocket2.Core.Session;
import JSocket2.Cryptography.EncryptionUtil;
import JSocket2.Utils.MessageUtil;

import javax.crypto.SecretKey;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.UUID;

public class MessageHandler {
    private final InputStream in;
    private final OutputStream out;
    private Session session;
    private static final int HEADER_SIZE = 35;
    private static final byte[] MAGIC_BYTES = new byte[] { 0x12, 0x34, 0x56, 0x78 };

    public MessageHandler(InputStream in, OutputStream out,Session session) {
        this.in = in;
        this.out = out;
        this.session = session;
    }
    private void syncToMagicBytes() throws IOException {
        byte[] buffer = new byte[MAGIC_BYTES.length];
        int index = 0;
        int b;

        while ((b = in.read()) != -1) {
            buffer[index % MAGIC_BYTES.length] = (byte) b;
            index++;

            if (index >= MAGIC_BYTES.length && matchesMagicBytes(buffer, index % MAGIC_BYTES.length)) {
                return;
            }
        }
        throw new EOFException("Stream closed before magic bytes were found.");
    }

    private boolean matchesMagicBytes(byte[] buffer, int startIndex) {
        for (int i = 0; i < MAGIC_BYTES.length; i++) {
            int bufferIndex = (startIndex + i) % MAGIC_BYTES.length;
            if (buffer[bufferIndex] != MAGIC_BYTES[i]) {
                return false;
            }
        }
        return true;
    }
    public Message read() throws IOException {
        syncToMagicBytes();
        MessageHeader header = readHeader();
        Message message = new Message(header);
        if(header.is_encrypted && header.type != MessageType.AES_KEY){
            byte[] ivBytes = readFully(16);
            message.setIvBytes(ivBytes);
        }
        readBody(message);
        if(header.is_encrypted && header.type != MessageType.AES_KEY) {
            MessageUtil.DecryptMessage(message, session.getAESKey());
        }
        return message;
    }

    public void readBody(Message message) throws IOException{

        if (message.header.metadata_length > 0) {
            byte[] metadata = readFully(message.header.metadata_length);
            message.setMetadata(metadata);
        }
        if (message.header.payload_length > 0) {
            byte[] payload = readFully(message.header.payload_length);
            message.setPayload(payload);
        }
    }
    public void writeBody(Message message)throws IOException {

        if (message.header.metadata_length > 0) {
            out.write(message.getMetadata());
        }
        if (message.header.payload_length > 0) {
            out.write(message.getPayload());
        }
    }
    public void write(Message message) throws IOException {
        if(message.header.is_encrypted && message.header.type != MessageType.AES_KEY){
            MessageUtil.EncryptMessage(message,session.getAESKey());
        }
        out.write(MAGIC_BYTES);
        writeHeader(message.header);
        if(message.header.is_encrypted && message.header.type != MessageType.AES_KEY){
            out.write(message.getIvBytes());
        }
        writeBody(message);
        out.flush();
    }
    private MessageHeader readHeader() throws IOException {
        byte[] headerBytes = readFully(HEADER_SIZE);
        ByteBuffer buffer = ByteBuffer.wrap(headerBytes);
        long mostSigBits = buffer.getLong();
        long leastSigBits = buffer.getLong();
        UUID uuid = new UUID(mostSigBits, leastSigBits);
        MessageType message_type = MessageType.fromCode(buffer.get());
        boolean is_need_ack = buffer.get() != 0;
        boolean is_encrypted = buffer.get() != 0;
        long timestamp = buffer.getLong();
        int metadata_length = buffer.getInt();
        int payload_length = buffer.getInt();

        return new MessageHeader(
                uuid,
                message_type,
                is_need_ack,
                is_encrypted,
                timestamp,
                metadata_length,
                payload_length
        );
    }
    private void writeHeader(MessageHeader header) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
        buffer.putLong(header.uuid.getMostSignificantBits());
        buffer.putLong(header.uuid.getLeastSignificantBits());
        buffer.put((byte) header.type.code);
        buffer.put((byte) (header.is_need_ack ? 1 : 0));
        buffer.put((byte) (header.is_encrypted ? 1 : 0));
        buffer.putLong(header.timestamp);
        buffer.putInt(header.metadata_length);
        buffer.putInt(header.payload_length);
        out.write(buffer.array());
    }
    private byte[] readFully(int len) throws IOException {
        byte[] buffer = new byte[len];
        int read = 0;
        while (read < len) {
            int r = in.read(buffer, read, len - read);
            if (r == -1) {
                throw new EOFException("Stream closed while reading " + len + " bytes. Only got " + read);
            }
            read += r;
        }
        return buffer;
    }

}
