import JSocket2.DI.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class DITest {

    @Test
    void shouldResolveScopedService() {
        ServiceCollection services = new ServiceCollection();
        services.AddScoped(TestService.class);

        ServiceProvider provider = services.CreateServiceProvider();

        TestService instance1 = provider.GetService(TestService.class);
        TestService instance2 = provider.GetService(TestService.class);

        assertNotNull(instance1);
        assertSame(instance1, instance2, "Scoped services should return same instance in same scope");
    }

    @Test
    void shouldResolveSingletonService() {
        ServiceCollection services = new ServiceCollection();
        services.AddSingleton(TestService.class);

        ServiceProvider provider = services.CreateServiceProvider();

        TestService instance1 = provider.GetService(TestService.class);
        TestService instance2 = provider.GetService(TestService.class);

        assertNotNull(instance1);
        assertSame(instance1, instance2, "Singleton services should return same instance across providers");
    }

    @Test
    void shouldResolveTransientService() {
        ServiceCollection services = new ServiceCollection();
        services.AddTransient(TestService.class);

        ServiceProvider provider = services.CreateServiceProvider();

        TestService instance1 = provider.GetService(TestService.class);
        TestService instance2 = provider.GetService(TestService.class);

        assertNotNull(instance1);
        assertNotSame(instance1, instance2, "Transient services should return different instances");
    }

    @Test
    void shouldResolveNestedDependencies() {
        ServiceCollection services = new ServiceCollection();
        services.AddTransient(DependentService.class);
        services.AddTransient(TestService.class);

        ServiceProvider provider = services.CreateServiceProvider();

        DependentService dependent = provider.GetService(DependentService.class);
        assertNotNull(dependent);
        assertNotNull(dependent.getTestService());
    }

    @Test
    void shouldResolveSingletonWithInstance() {
        TestService preBuiltInstance = new TestService();
        ServiceCollection services = new ServiceCollection();
        services.AddSingletonWithInstance(TestService.class, preBuiltInstance);

        ServiceProvider provider = services.CreateServiceProvider();
        TestService resolvedInstance = provider.GetService(TestService.class);

        assertSame(preBuiltInstance, resolvedInstance);
    }

    @Test
    void shouldDetectCircularDependency() {
        ServiceCollection services = new ServiceCollection();
        services.AddTransient(CircularServiceA.class);
        services.AddTransient(CircularServiceB.class);

        ServiceProvider provider = services.CreateServiceProvider();

        assertThrows(CircularDependencyException.class, () -> {
            provider.GetService(CircularServiceA.class);
        });
    }

    @Test
    void shouldSelectAnnotatedConstructor() {
        ServiceCollection services = new ServiceCollection();
        services.AddTransient(ServiceWithMultipleConstructors.class);
        services.AddTransient(TestService.class);

        ServiceProvider provider = services.CreateServiceProvider();
        ServiceWithMultipleConstructors service = provider.GetService(ServiceWithMultipleConstructors.class);

        assertNotNull(service);
        assertNotNull(service.getDependency());
    }

    @Test
    void shouldHandleScopedServicesInMultipleThreads() throws InterruptedException {
        ServiceCollection services = new ServiceCollection();
        services.AddScoped(TestService.class);

        ServiceProvider provider = services.CreateServiceProvider();

        TestService[] instances = new TestService[2];

        Thread thread1 = new Thread(() -> {
            instances[0] = provider.GetService(TestService.class);
        });

        Thread thread2 = new Thread(() -> {
            instances[1] = provider.GetService(TestService.class);
        });

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        assertNotNull(instances[0]);
        assertNotNull(instances[1]);
        assertNotSame(instances[0], instances[1]);
    }
    @Test
    void shouldResolveSingletonServiceByInstance(){
        ServiceCollection services = new ServiceCollection();
        TestService instance = new TestService();
        services.AddSingletonWithInstance(TestService.class,instance);

        ServiceProvider provider = services.CreateServiceProvider();

        TestService instance1 = provider.GetService(TestService.class);

        assertNotNull(instance1);
        assertSame(instance, instance1, "Singleton services should return same instance across providers");
    }
    static class TestService {
        public TestService() {}
    }

    static class DependentService {
        private final TestService testService;

        public DependentService(TestService testService) {
            this.testService = testService;
        }

        public TestService getTestService() {
            return testService;
        }
    }

    static class CircularServiceA {
        public CircularServiceA(CircularServiceB b) {}
    }

    static class CircularServiceB {
        public CircularServiceB(CircularServiceA a) {}
    }

    static class ServiceWithMultipleConstructors {
        private final TestService dependency;

        public ServiceWithMultipleConstructors() {
            this.dependency = null;
        }

        @Inject
        public ServiceWithMultipleConstructors(TestService dependency) {
            this.dependency = dependency;
        }

        public TestService getDependency() {
            return dependency;
        }
    }
}