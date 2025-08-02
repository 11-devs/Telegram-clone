package JSocket2.DI;

public class ServiceDescriptor {
    public final Class<?> serviceType;
    public final Class<?> implementationType;
    public final ServiceLifetime lifetime;
    public final Object instance;
    public ServiceDescriptor(Class<?> serviceType ,ServiceLifetime lifetime,Object instance){
        this.serviceType = serviceType;
        this.implementationType  = null;
        this.lifetime = lifetime;
        this.instance = instance;
    }
    public ServiceDescriptor(Class<?> serviceType, Class<?> implementationType ,ServiceLifetime lifetime){
        this.serviceType = serviceType;
        this.implementationType  = implementationType;
        this.lifetime = lifetime;
        this.instance = null;
    }

}
