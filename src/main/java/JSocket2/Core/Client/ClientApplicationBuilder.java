package JSocket2.Core.Client;

import JSocket2.DI.ServiceCollection;
import JSocket2.Protocol.EventHub.EventSubscriberBase;
import JSocket2.Protocol.EventHub.EventSubscriberCollection;
import JSocket2.Protocol.IConnectionEventListener;

public class ClientApplicationBuilder {
    private String host;
    private int port;
    private final ServiceCollection services;
    private final EventSubscriberCollection subscribers;
    private IConnectionEventListener connectionEventListener;
    public ClientApplicationBuilder(){
        services = new ServiceCollection();
        subscribers = new EventSubscriberCollection();
    }
    public ClientApplicationBuilder setConnectionEventListener(IConnectionEventListener connectionEventListener){
        this.connectionEventListener = connectionEventListener;
        return this;
    }
    public ClientApplicationBuilder setHost(String host){
        this.host = host;
        return this;
    }
    public ClientApplicationBuilder setPort(int port){
        this.port = port;
        return this;
    }
    public ServiceCollection getServices(){
        return services;
    }
    public ClientApplicationBuilder addEventSubscriber(Class<?> subscribeType){
        subscribers.subscribe(subscribeType);
        return this;
    }
    public ClientApplication Build(){
        return new ClientApplication(host,port,connectionEventListener,subscribers,services);
    }
}
