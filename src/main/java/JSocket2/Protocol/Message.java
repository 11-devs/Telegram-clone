package JSocket2.Protocol;

public class Message {
    public final MessageHeader header;
    private byte[] ivBytes = new byte[0];
    private byte[] metadata = new byte[0];
    private byte[] payload = new byte[0];
    public Message(MessageHeader header){
        this.header = header;
    }
    public Message(MessageHeader header, byte[] metadata,byte[] payload){
        this.header = header;
        this.metadata = metadata;
        this.payload = payload;
    }
    public Message(MessageHeader header, byte[] metadata,byte[] payload,byte[] ivBytes){
        this.header = header;
        this.metadata = metadata;
        this.payload = payload;
        this.ivBytes = ivBytes;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
        header.payload_length = payload.length;
    }

    public byte[] getPayload() {
        return payload;
    }

    public byte[] getMetadata() {
        return metadata;
    }

    public void setMetadata(byte[] metadata) {
        this.metadata = metadata;
        header.metadata_length = metadata.length;
    }

    public byte[] getIvBytes() {
        return ivBytes;
    }

    public void setIvBytes(byte[] ivBytes) {
        this.ivBytes = ivBytes;
    }
}
