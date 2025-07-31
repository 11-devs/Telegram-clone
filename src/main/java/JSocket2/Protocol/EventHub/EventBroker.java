package JSocket2.Protocol.EventHub;

import JSocket2.DI.ServiceProvider;
import com.google.gson.Gson;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventBroker {
    public EventBroker(ServiceProvider provider, Map<String, List<Class<?>>> subscribers) {
        this.provider = provider;
        this.subscribers = subscribers;
    }

    private final Map<String, List<Class<?>>> subscribers;
    private final ServiceProvider provider;

    private final Gson gson = new Gson();


    private void invokeMethod(Method method, Object subscriber, String payloadJson) throws Exception {
        Class<?>[] parameterTypes = method.getParameterTypes();

        if (parameterTypes.length == 0) {
            method.invoke(subscriber);
        } else {
            Object param = gson.fromJson(payloadJson, parameterTypes[0]);
            method.invoke(subscriber, param);
        }
    }
    public void publish(EventMetadata metadata, String payloadJson) {
        String eventName = metadata.getEventName().toLowerCase();
        List<Class<?>> subscriberTypes = subscribers.get(eventName);

        //if (subscriberTypes == null || subscriberTypes.isEmpty()) {

        subscriberTypes.parallelStream().forEach(subscriberType -> {
            Object subscriber = provider.GetService(subscriberType);
            if (subscriber == null) {
                //throw new SubscriberNotRegisteredException(subscriberType.getName());
            }

            try {
                for (Method method : subscriberType.getMethods()) {
                    if (isEventHandlerMethod(method, eventName)) {
                        invokeMethod(method, subscriber, payloadJson);
                    }
                }
            } catch (Exception e) {
                //throw new EventHandlingException("Error handling event in subscriber: " + subscriberType.getName(), e);
            }
        });
    }


    private static boolean isEventHandlerMethod(Method method, String eventName) {
        return method.isAnnotationPresent(OnEvent.class) && method.getAnnotation(OnEvent.class).value().toLowerCase().equals(eventName);
    }
}
