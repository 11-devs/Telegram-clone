package JSocket2.Protocol;

import java.io.IOException;

public interface IMessageProcessor {
    void Invoke(Message message) throws IOException;
}
