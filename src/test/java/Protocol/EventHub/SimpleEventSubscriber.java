package Protocol.EventHub;

import JSocket2.Protocol.EventHub.EventSubscriberBase;
import JSocket2.Protocol.EventHub.OnEvent;

public class SimpleEventSubscriber extends EventSubscriberBase {

    public int onWithModelSumEventResult = 0;
    public int onWithoutModelSumEventResult = 0;
    public int[] multipleEventResult = new int[4];
    public int IgnoreEventNameCaseResult;
    @OnEvent("withModelSumEvent")
    public void onWithModelSumEvent(SimpleSumEventModel model){
        onWithModelSumEventResult = model.a + model.b;
    }
    @OnEvent("withoutModelSumEvent")
    public void onWithoutModelSumEvent(int a, int b){
        onWithoutModelSumEventResult = a + b;
    }
    @OnEvent("multipleEvent")
    public void multipleEvent1(int a, int b){
        multipleEventResult[0] = a + b;
    }
    @OnEvent("multipleEvent")
    public void multipleEvent2(int a, int b){
        multipleEventResult[1] = a - b;
    }
    @OnEvent("multipleEvent")
    public void multipleEvent3(int a, int b){
        multipleEventResult[2] = a * b;
    }
    @OnEvent("multipleEvent")
    public void multipleEvent4(int a, int b){
        multipleEventResult[3] = a / b;
    }
    @OnEvent("ignoreEventNameCase")
    public void IgnoreEventNameCase(int a){
        IgnoreEventNameCaseResult = a;
    }
    @OnEvent("ignoreWrongSubscriberEvent")
    public void ignoreWrongSubscriberEvent(int a){

    }
}
