package JSocket2.Protocol;

public enum StatusCode {
    OK(200),
    BAD_REQUEST(400),
    NOT_FOUND(404);

    public final int code;
    StatusCode(int code) {
        this.code = code;
    }
    public static StatusCode fromCode(int code) {
        for (StatusCode type : values()) {
            if (type.code == code)
                return type;
        }
        throw new IllegalArgumentException("Unknown MessageType code: " + code);
    }
}
