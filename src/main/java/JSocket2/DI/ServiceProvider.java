package JSocket2.DI;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceProvider {
    private final Map<Class<?>, ServiceDescriptor> descriptors;
    private final Map<Class<?>, Object> singletonInstances = new ConcurrentHashMap<>();
    private final ThreadLocal<Map<Class<?>, Object>> scopedInstances = ThreadLocal.withInitial(ConcurrentHashMap::new);
    private final ThreadLocal<Set<Class<?>>> creatingStack = ThreadLocal.withInitial(HashSet::new);
    private final ThreadLocal<Boolean> scopeActive = ThreadLocal.withInitial(() -> false);

    public <T> T GetService(Class<T> service) {
        ServiceDescriptor descriptor = descriptors.get(service);
        if (descriptor == null) {
            throw new IllegalArgumentException("Service not registered: " + service.getName());
        }
        if (descriptor.lifetime == ServiceLifetime.SCOPED && !scopeActive.get()) {
            scopeActive.set(true);
        }
        return resolveService(descriptor);
    }

    public ServiceProvider(Map<Class<?>, ServiceDescriptor> descriptors) {
        this.descriptors = new ConcurrentHashMap<>(descriptors);
    }

    private <T> T resolveService(ServiceDescriptor descriptor) {
        switch (descriptor.lifetime) {
            case SCOPED:
                return resolveScopedService(descriptor);
            case SINGLETON:
                return resolveSingletonService(descriptor);
            case TRANSIENT:
                return createNewInstance(descriptor);
            default:
                throw new IllegalArgumentException("Unknown lifetime: " + descriptor.lifetime);
        }
    }

    private <T> T resolveScopedService(ServiceDescriptor descriptor) {
        Map<Class<?>, Object> scopeMap = scopedInstances.get();
        return (T) scopeMap.computeIfAbsent(descriptor.serviceType,
                k -> createNewInstance(descriptor));
    }

    @SuppressWarnings("unchecked")
    private <T> T resolveSingletonService(ServiceDescriptor descriptor) {
        if (singletonInstances.containsKey(descriptor.serviceType)) {
            return (T) singletonInstances.get(descriptor.serviceType);
        }

        synchronized (this) {
            if (singletonInstances.containsKey(descriptor.serviceType)) {
                return (T) singletonInstances.get(descriptor.serviceType);
            }

            Object instance;
            if (descriptor.instance != null) {
                instance = descriptor.instance;
            } else {
                instance = createNewInstance(descriptor);
            }

            singletonInstances.put(descriptor.serviceType, instance);
            return (T) instance;
        }
    }

    private <T> T createNewInstance(ServiceDescriptor descriptor) {
        Class<?> implementationType = descriptor.implementationType;

        Set<Class<?>> stack = creatingStack.get();
        if (stack.contains(implementationType)) {
            throw new CircularDependencyException(
                    "Circular dependency detected: " + stack + " -> " + implementationType
            );
        }

        stack.add(implementationType);
        try {
            return (T) createInstance(implementationType);
        } finally {
            stack.remove(implementationType);
        }
    }

    private <T> T createInstance(Class<T> implementationType) {
        try {
            Constructor<?>[] constructors = implementationType.getConstructors();
            Constructor<?> constructor = selectConstructor(constructors);
            constructor.setAccessible(true);
            Class<?>[] paramTypes = constructor.getParameterTypes();
            Object[] params = new Object[paramTypes.length];

            for (int i = 0; i < paramTypes.length; i++) {
                params[i] = GetService(paramTypes[i]);
            }
            var instance = constructor.newInstance(params);
            var castedInstance = implementationType.cast(instance);
            return castedInstance;
        }
        catch(CircularDependencyException e){
            throw e;
        }
        catch (Exception e) {
            throw new ServiceCreationException(
                    "Failed to create instance of " + implementationType.getName()
            );
        }
    }

    private Constructor<?> selectConstructor(Constructor<?>[] constructors) {
        if (constructors.length == 1) {
            return constructors[0];
        }

        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                return constructor;
            }
        }

        throw new ServiceCreationException("Multiple constructors found but no @Inject annotation specified");
    }
}