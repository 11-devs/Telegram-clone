package JSocket2.Protocol.Rpc;

import JSocket2.DI.ServiceProvider;

import java.util.HashMap;
import java.util.Map;

public class RpcControllerCollection {
    private final Map<String, Class<?>> controllers = new HashMap<>();
    private final Map<String, Class<?>> modelMap = new HashMap<>();
    public <T> void registerController(Class<?> controllerType) {
        String controllerName = controllerType.getSimpleName().toLowerCase();
        if(controllerType.isAnnotationPresent(RpcController.class))
        {
            var rpcController = controllerType.getAnnotation(RpcController.class);
            if(!rpcController.Name().isEmpty()) {
                controllerName = rpcController.Name().toLowerCase();
            }
        }
        if (!controllers.containsKey(controllerName)) {
            controllers.put(controllerName, controllerType);
        } else if (controllers.get(controllerName) != controllerType) {
            throw new IllegalStateException("Controller '" + controllerName + "' is already registered with a different instance.");
        }
    }
    public <T> void registerActions(Class<T> controllerType) {
        var methods = controllerType.getMethods();
        for (var method : methods){
            if(method.getReturnType() == RpcResponse.class && method.getParameterCount() <= 1){
                String actionName = method.getName();
                if(method.isAnnotationPresent(RpcAction.class)){
                    var controllerAction = method.getAnnotation(RpcAction.class);
                    if(!controllerAction.Name().isEmpty()){
                        actionName = controllerAction.Name();
                    }
                }
                Class<?> modelType = null;
                if(method.getParameterCount()!= 0){
                    modelType = method.getParameterTypes()[0];
                }
                registerAction(controllerType,actionName,modelType);
            }
        }
    }
    public <T> void registerAction(Class<?> controllerType, String action, Class<T> modelType) {
        String routeKey = (controllerType.getSimpleName() + "/" + action).toLowerCase();
        if (modelMap.containsKey(routeKey)) {
            throw new IllegalStateException("Route '" + routeKey + "' is already registered.");
        }
        modelMap.put(routeKey, modelType);
    }

    public RpcDispatcher CreateRpcDispatcher(ServiceProvider provider) {
        return new RpcDispatcher(provider,controllers,modelMap);
    }
}
