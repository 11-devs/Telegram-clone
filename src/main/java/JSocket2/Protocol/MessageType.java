package JSocket2.Protocol;

public enum MessageType {
    RPC_CALL(10),
    RPC_RESPONSE(11),
    UPLOAD_REQUEST(20),
    UPLOAD_CHUNK(21),
    UPLOAD_ACK(23),
    UPLOAD_RESUME_REQUEST(24),
    DOWNLOAD_REQUEST(30),
    DOWNLOAD_START(34),
    RSA_PUBLIC_KEY(40),
    AES_KEY(41),
    CHANGE_AES_KEY_REQUEST(42),
    SEND_CHUNK(51),
    RECEIVE_CHUNK_ACK(52),
    AUTH(60),
    EVENT(70);
    public final int code;
    MessageType(int code) {
        this.code = code;
    }

    public static MessageType fromCode(int code) {
        for (MessageType type : values()) {
            if (type.code == code)
                return type;
        }
        throw new IllegalArgumentException("Unknown MessageType code: " + code);
    }
}
