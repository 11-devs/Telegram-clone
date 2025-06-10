package JSocket.Protocol;

import java.io.IOException;

public interface IMessageProcessor {
    void Invoke(Message message) throws IOException;
}
