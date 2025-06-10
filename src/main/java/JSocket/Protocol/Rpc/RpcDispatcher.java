package JSocket.Protocol.Rpc;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Method;
public class RpcDispatcher {

    public RpcDispatcher() {}

    private final Map<String, Object> controllers = new HashMap<>();
    private final Map<String, Class<?>> modelMap = new HashMap<>();
    private final Gson gson = new Gson();

    public <T> void registerController(String controller, Object controllerInstance) {
        if (!controllers.containsKey(controller)) {
            controllers.put(controller, controllerInstance);
        } else if (controllers.get(controller) != controllerInstance) {
            throw new IllegalStateException("Controller '" + controller + "' is already registered with a different instance.");
        }
    }
    public <T> void registerAction(String controller, String action, Class<T> modelType) {
        String routeKey = controller + "/" + action;
        if (modelMap.containsKey(routeKey)) {
            throw new IllegalStateException("Route '" + routeKey + "' is already registered.");
        }
        modelMap.put(routeKey, modelType);
    }

    public RpcResponse<?> dispatch(RpcCallMetadata metadata, String payload_json) {
        String key = metadata.getController() + "/" + metadata.getAction();
        Object controller = controllers.get(metadata.getController());
        if (controller == null) throw new RuntimeException("Controller not registered: " + metadata.getController());

        Class<?> modelType = modelMap.get(key);
        if (modelType == null) throw new RuntimeException("No model found for route: " + key);
        Object methodModel = gson.fromJson(payload_json, modelType);
        try {
            Method method = controller.getClass().getMethod(metadata.getAction(), modelType);
            return (RpcResponse<?>) method.invoke(controller, methodModel);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Action method not found: " + metadata.getAction());
        } catch (Exception e) {
            throw new RuntimeException("Error invoking method: " + e.getMessage(), e);
        }
    }
}
