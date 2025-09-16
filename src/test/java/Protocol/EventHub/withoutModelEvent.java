package Protocol.EventHub;

import JSocket2.Core.Server.ServerSessionManager;
import JSocket2.Protocol.EventHub.EventBase;

import java.io.IOException;

public class withoutModelEvent extends EventBase {
    @Override
    public void Invoke(ServerSessionManager serverSessionManager,String receiverId, Object... args) throws IOException {

    }
}
