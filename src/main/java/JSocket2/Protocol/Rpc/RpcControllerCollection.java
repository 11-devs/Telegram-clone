package JSocket2.Protocol.Rpc;

import JSocket2.DI.ServiceProvider;

import java.util.HashMap;
import java.util.Map;

public class RpcControllerCollection {
    private final Map<String, Class<?>> controllers = new HashMap<>();
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

    public RpcDispatcher CreateRpcDispatcher(ServiceProvider provider) {
        return new RpcDispatcher(provider,controllers);
    }
}
