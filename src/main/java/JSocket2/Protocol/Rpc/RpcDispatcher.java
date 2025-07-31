package JSocket2.Protocol.Rpc;

import JSocket2.DI.ServiceProvider;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Method;
public class RpcDispatcher {

    public RpcDispatcher(ServiceProvider provider,Map<String, Class<?>> controllers,Map<String, Class<?>> modelMap) {
        this.provider = provider;
        this.controllers = controllers;
        this.modelMap = modelMap;
    }

    private final Map<String, Class<?>> controllers;
    private final Map<String, Class<?>> modelMap;
    private final ServiceProvider provider;

    private final Gson gson = new Gson();



    public RpcResponse<?> dispatch(RpcCallMetadata metadata, String payload_json) {
        String controllerName =  metadata.getController().toLowerCase();
        String ActionName = metadata.getAction().toLowerCase();
        String key = controllerName + "/" + ActionName;
        Class<?> controllerType = controllers.get(controllerName);
        Object controller = provider.GetService(controllerType);
        if (controller == null) throw new RuntimeException("Controller not registered: " + controllerName);
        Class<?> modelType = modelMap.get(key);
        try {
            if(modelType == null){
                Method method = controller.getClass().getMethod(ActionName);
                return (RpcResponse<?>) method.invoke(controller);
            }else{
                Object methodModel = gson.fromJson(payload_json, modelType);
                Method method = controller.getClass().getMethod(ActionName, modelType);
                return (RpcResponse<?>) method.invoke(controller, methodModel);
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Action method not found: " + ActionName);
        } catch (Exception e) {
            throw new RuntimeException("Error invoking method: " + e.getMessage(), e);
        }
    }
}
