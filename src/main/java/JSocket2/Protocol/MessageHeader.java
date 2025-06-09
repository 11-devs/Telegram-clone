package JSocket2.Protocol;

import java.util.UUID;

public class MessageHeader {
    public final UUID uuid;
    public final MessageType type;
    public final boolean is_need_ack;
    public final boolean is_encrypted;
    public final long timestamp;
    public int metadata_length;
    public int payload_length;

    public MessageHeader(UUID uuid,MessageType type, boolean isNeedAck, boolean isEncrypted, int metadataLength, int payloadLength){

        this.uuid = uuid;
        this.type = type;
        is_need_ack = isNeedAck;
        is_encrypted = isEncrypted;
        this.timestamp = System.currentTimeMillis();
        metadata_length = metadataLength;
        payload_length = payloadLength;
    }
    public MessageHeader(UUID uuid,MessageType type, boolean isNeedAck, boolean isEncrypted, long timestamp,int metadataLength, int payloadLength){

        this.uuid = uuid;
        this.type = type;
        is_need_ack = isNeedAck;
        is_encrypted = isEncrypted;
        this.timestamp = timestamp;
        metadata_length = metadataLength;
        payload_length = payloadLength;
    }
    public MessageHeader(UUID uuid,MessageType type, boolean isNeedAck, boolean isEncrypted){

        this.uuid = uuid;
        this.type = type;
        is_need_ack = isNeedAck;
        is_encrypted = isEncrypted;
        this.timestamp = System.currentTimeMillis();

    }
    public static MessageHeader BuildRpcResponseHeader(UUID uuid,boolean is_encrypted,int metadataLength, int payloadLength){
        return new MessageHeader(uuid,MessageType.RPC_RESPONSE,false,is_encrypted,metadataLength,payloadLength);
    }
    public static MessageHeader BuildRpcCallHeader(UUID uuid,boolean is_encrypted,int metadataLength, int payloadLength){
        return new MessageHeader(uuid,MessageType.RPC_CALL,false,is_encrypted,metadataLength,payloadLength);
    }

    public static MessageHeader BuildUploadAckHeader(UUID uuid,int metadataLength) {
        return new MessageHeader(uuid,MessageType.UPLOAD_ACK,false,false,metadataLength,4);
    }

    public static MessageHeader BuildUploadRequestHeader(UUID uuid,boolean is_encrypted,int metadataLength, int payloadLength){
        return new MessageHeader(uuid,MessageType.UPLOAD_REQUEST,false,is_encrypted,metadataLength,payloadLength);
    }
    public static MessageHeader BuildDownloadRequestHeader(UUID uuid,boolean is_encrypted,int metadataLength, int payloadLength){

        return new MessageHeader(uuid,MessageType.DOWNLOAD_REQUEST,false,is_encrypted,metadataLength,payloadLength);
    }
    public static MessageHeader BuildStartDownloadRequestHeader(UUID uuid,boolean is_encrypted,int metadataLength, int payloadLength){

        return new MessageHeader(uuid,MessageType.DOWNLOAD_START,false,is_encrypted,metadataLength,payloadLength);
    }

    public static MessageHeader BuildUploadChunkHeader(UUID uuid,boolean is_encrypted,int metadataLength, int payloadLength) {
        return new MessageHeader(uuid,MessageType.UPLOAD_CHUNK,false,is_encrypted,metadataLength,payloadLength);
    }

    public static MessageHeader BuildSendChunkHeader(UUID uuid,boolean is_encrypted,int metadataLength, int payloadLength) {
        return new MessageHeader(uuid,MessageType.SEND_CHUNK,false,is_encrypted,metadataLength,payloadLength);
    }
    public static MessageHeader BuildReceiveChunkAckHeader(UUID uuid,int metadataLength) {

        return new MessageHeader(uuid,MessageType.RECEIVE_CHUNK_ACK,false,false,metadataLength,4);
    }
    public static MessageHeader BuildResumeUploadRequestAckHeader(UUID uuid, boolean is_encrypted, int metadataLength, int payloadLength) {
        return new MessageHeader(uuid,MessageType.UPLOAD_RESUME_REQUEST,false,is_encrypted,metadataLength,payloadLength);
    }
    public static MessageHeader BuildRsaPublicKeyHeader(UUID uuid, int payloadLength) {

        return new MessageHeader(uuid, MessageType.RSA_PUBLIC_KEY, true, false, 0, payloadLength);
    }
    public static MessageHeader BuildAesKeyHeader(UUID uuid, int payloadLength) {
        return new MessageHeader(uuid,MessageType.AES_KEY,true,true,0,payloadLength);
    }
    public static MessageHeader BuildAuthHeader(UUID uuid, int payloadLength) {
        return new MessageHeader(uuid,MessageType.AUTH,true,true,0,payloadLength);
    }
}
