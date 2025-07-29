package JSocket2.DI;

public class ServiceDescriptor {
    public final Class<?> serviceType;
    public final Class<?> implementationType ;
    public final ServiceLifetime lifetime;
    public ServiceDescriptor(Class<?> serviceType, Class<?> implementationType ,ServiceLifetime lifetime){
        this.serviceType = serviceType;
        this.implementationType  = implementationType;
        this.lifetime = lifetime;
    }

}
