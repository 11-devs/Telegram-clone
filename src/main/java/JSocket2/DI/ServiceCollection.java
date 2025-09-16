package JSocket2.DI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceCollection {
    private Map<Class<?>,ServiceDescriptor> descriptors = new HashMap<>();
    public ServiceCollection AddScoped(Class<?> serviceType,Class<?> implementationType){
        descriptors.put(serviceType,new ServiceDescriptor(serviceType,implementationType,ServiceLifetime.SCOPED));
        return this;
    };
    public <T> ServiceCollection AddSingletonWithInstance(Class<?> serviceType, T instance){
        descriptors.put(serviceType,new ServiceDescriptor(serviceType,ServiceLifetime.SINGLETON,instance));
        return this;
    };
    public ServiceCollection AddSingleton(Class<?> serviceType,Class<?> implementationType){
        descriptors.put(serviceType,new ServiceDescriptor(serviceType,implementationType,ServiceLifetime.SINGLETON));
        return this;
    };
    public ServiceCollection AddTransient(Class<?> serviceType,Class<?> implementationType){
        descriptors.put(serviceType,new ServiceDescriptor(serviceType,implementationType,ServiceLifetime.TRANSIENT));
        return this;
    };
    public ServiceCollection AddScoped(Class<?> serviceType){
        descriptors.put(serviceType,new ServiceDescriptor(serviceType,serviceType,ServiceLifetime.SCOPED));
        return this;
    };
    public ServiceCollection AddSingleton(Class<?> serviceType){
        descriptors.put(serviceType,new ServiceDescriptor(serviceType,serviceType,ServiceLifetime.SINGLETON));
        return this;
    };
    public ServiceCollection AddTransient(Class<?> serviceType){
        descriptors.put(serviceType,new ServiceDescriptor(serviceType,serviceType,ServiceLifetime.TRANSIENT));
        return this;
    };
    public ServiceProvider CreateServiceProvider(){
        return new ServiceProvider(descriptors);
    }
}
