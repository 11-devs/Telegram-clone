package JSocket2.Protocol.Authentication;

public class InvalidAccessKeyException extends AuthException {
    public InvalidAccessKeyException(String message) {
        super(message);
    }
    public InvalidAccessKeyException(){
        super("Access Key is not valid");
    }
}
