package JSocket2.Protocol.EventHub;

import JSocket2.DI.ServiceProvider;
import com.google.gson.Gson;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
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

    public void publish(EventMetadata metadata, String payloadJson) {
        String eventName = metadata.getEventName().toLowerCase();
        List<Class<?>> subscriberTypes = subscribers.get(eventName);

        //if (subscriberTypes == null || subscriberTypes.isEmpty()) {

        subscriberTypes.parallelStream().forEach(subscriberType -> {
            Object subscriber = provider.GetService(subscriberType);
            //if (subscriber == null) {
                //throw new SubscriberNotRegisteredException(subscriberType.getName());
            //}

            try {
                Object[] rawParameters = gson.fromJson(payloadJson,Object[].class);
                var matchingMethods = findMatchingMethod(subscriberType,eventName,rawParameters);
                for (var methodsEntry : matchingMethods.entrySet()){
                    var method = methodsEntry.getKey();
                    var parameters = methodsEntry.getValue();
                    method.invoke(subscriber,parameters);
                }
            } catch (Exception e) {
                throw new EventHandlingException("Error handling event in subscriber: " + subscriberType.getName(), e);
            }
        });
    }
    private Map<Method, Object[]> findMatchingMethod(Class<?> subscriberClass, String eventName, Object[] parameters) {
        var matchingMethods = new HashMap<Method, Object[]>();
        Method[] methods = subscriberClass.getMethods();

        for (Method method : methods) {
            if (isEventHandlerMethod(method, eventName)) {
                Class<?>[] paramTypes = method.getParameterTypes();
                if (paramTypes.length == parameters.length) {
                    try {
                        Object[] convertedParams = new Object[parameters.length];
                        for (int i = 0; i < parameters.length; i++) {
                            convertedParams[i] = gson.fromJson(
                                    gson.toJson(parameters[i]),
                                    paramTypes[i]
                            );
                        }
                        matchingMethods.put(method,convertedParams);
                    } catch (Exception e) {
                        continue;
                    }
                }
            }
        }
        return matchingMethods;
    }

    private static boolean isEventHandlerMethod(Method method, String eventName) {
        return method.isAnnotationPresent(OnEvent.class) && method.getAnnotation(OnEvent.class).value().toLowerCase().equals(eventName);
    }
}
