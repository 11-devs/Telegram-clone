package JSocket2.Protocol.EventHub;

import JSocket2.DI.ServiceProvider;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventSubscriberCollection {
    private final Map<String, List<Class<?>>> subscribers = new ConcurrentHashMap<>();
    public EventSubscriberCollection(){

    }
    public <TSubscriber> void subscribe(Class<TSubscriber> subscribeType){
        for (var method:subscribeType.getMethods()){
            if(method.isAnnotationPresent(OnEvent.class)){
                var onEventAnnotation = method.getAnnotation(OnEvent.class);
                if(!onEventAnnotation.value().isEmpty()) subscribe(onEventAnnotation.value(),subscribeType);
            }
        }
    }
    private <TEvent,TSubscriber> void subscribe(String eventName,Class<TSubscriber> subscribeType){
        subscribers.computeIfAbsent(eventName.toLowerCase(),k -> new CopyOnWriteArrayList<>()).
                add(subscribeType);
    }
    public EventBroker CreateEventBroker(ServiceProvider provider){
        return new EventBroker(provider,subscribers);
    }
}
