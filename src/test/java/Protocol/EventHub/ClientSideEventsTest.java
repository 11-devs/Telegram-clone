package Protocol.EventHub;

import JSocket2.DI.ServiceCollection;
import JSocket2.DI.ServiceProvider;
import JSocket2.Protocol.EventHub.*;
import com.google.gson.Gson;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClientSideEventsTest {
    ServiceProvider serviceProvider;
    EventBroker eventBroker;
    EventSubscriberCollection eventSubscriberCollection = new EventSubscriberCollection();
    Gson gson = new Gson();

    @BeforeEach
    void setup() {
        var services = new ServiceCollection();
        services.AddSingleton(SimpleEventSubscriber.class);
        eventSubscriberCollection.subscribe(SimpleEventSubscriber.class);
        serviceProvider = services.CreateServiceProvider();
        eventBroker = eventSubscriberCollection.CreateEventBroker(serviceProvider);
    }

    @Test
    void publishEventWithModel_ShouldCorrectlyHandleSumOperation() {
        var metadata = new EventMetadata("withModelSumEvent");
        var model = new Object[]{new SimpleSumEventModel(5,6)};
        eventBroker.publish(metadata, gson.toJson(model));

        var subscriber = serviceProvider.GetService(SimpleEventSubscriber.class);
        assertEquals(11, subscriber.onWithModelSumEventResult);
    }

    @Test
    void publishEventWithoutModel_ShouldCorrectlySumParameters() {
        var metadata = new EventMetadata("withoutModelSumEvent");
        var model = new Object[]{5,6};
        eventBroker.publish(metadata, gson.toJson(model));

        var subscriber = serviceProvider.GetService(SimpleEventSubscriber.class);
        assertEquals(11, subscriber.onWithoutModelSumEventResult);
    }

    @Test
    void publishMultipleOperationsEvent_ShouldReturnAllResults() {
        var metadata = new EventMetadata("multipleEvent");
        var model = new Object[]{10,5};
        eventBroker.publish(metadata, gson.toJson(model));

        var subscriber = serviceProvider.GetService(SimpleEventSubscriber.class);
        assertArrayEquals(new int[]{15,5,50,2}, subscriber.multipleEventResult);
    }

    @Test
    void publishEvent_ShouldIgnoreEventNameCase() {
        var metadata = new EventMetadata("iGnoReEveNtNameCaSe");
        var model = new Object[]{2};
        eventBroker.publish(metadata, gson.toJson(model));

        var subscriber = serviceProvider.GetService(SimpleEventSubscriber.class);
        assertEquals(2, subscriber.IgnoreEventNameCaseResult);
    }

    @Test
    void publishEventWithWrongSubscriber_ShouldNotThrowException() {
        var metadata = new EventMetadata("ignoreWrongSubscriberEvent");
        var model = new Object[]{2,3};

        assertDoesNotThrow(() -> eventBroker.publish(metadata, gson.toJson(model)));
    }
}