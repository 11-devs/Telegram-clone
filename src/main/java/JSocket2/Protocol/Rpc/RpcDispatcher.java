package JSocket2.Protocol.Rpc;

import JSocket2.Core.Server.ServerSession;
import JSocket2.DI.ServiceProvider;
import JSocket2.Protocol.Authentication.UserIdentity;
import com.google.gson.Gson;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Method;
public class RpcDispatcher {

    public RpcDispatcher(ServiceProvider provider,Map<String, Class<?>> controllers) {
        this.provider = provider;
        this.controllers = controllers;
    }

    private final Map<String, Class<?>> controllers;
    private final ServiceProvider provider;

    private final Gson gson = new Gson();
    public RpcResponse<?> dispatch(RpcCallMetadata metadata, String payload_json, UserIdentity activeUser) {
        String controllerName =  metadata.getController().toLowerCase();
        String ActionName = metadata.getAction().toLowerCase();
        Class<?> controllerType = controllers.get(controllerName);
        Object controller = provider.GetService(controllerType);
        if (controller == null) throw new RuntimeException("Controller not registered: " + controllerName);
        if(controller instanceof RpcControllerBase rpcController){
            rpcController.setCurrentUser(activeUser);
        Object[] methodModels = gson.fromJson(payload_json, Object[].class);
        try {
            if(methodModels == null){
                Method method = controller.getClass().getMethod(ActionName);
                return (RpcResponse<?>) method.invoke(controller);
            }else{

                var methodMap = findMatchingMethod(controllerType,ActionName,methodModels);
                return (RpcResponse<?>) methodMap.getKey().invoke(controller, Arrays.asList(methodMap.getValue()).toArray());
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Action method not found: " + ActionName);
        } catch (Exception e) {
            throw new RuntimeException("Error invoking method: " + e.getMessage(), e);
        }
        }
        throw new RuntimeException("Class must be a inheritance of RpcControllerBase: " + ActionName);
    }
    private Map.Entry<Method, Object[]> findMatchingMethod(Class<?> controllerClass, String methodName, Object[] parameters)
            throws NoSuchMethodException {

        Method[] methods = controllerClass.getMethods();

        for (Method method : methods) {
            if (method.getName().toLowerCase().equals(methodName)) {
                Class<?>[] paramTypes = method.getParameterTypes();

                if (paramTypes.length == parameters.length) {
                    try {
                        Object[] convertedParams = new Object[parameters.length];
                        for (int i = 0; i < parameters.length; i++) {
                            convertedParams[i] = gson.fromJson(
                                    gson.toJson(parameters[i]),
                                    paramTypes[i]
                            );
                        }
                        return Map.entry(method, convertedParams);
                    } catch (Exception e) {
                        continue;
                    }
                }
            }
        }

        throw new NoSuchMethodException("No suitable method found");
    }
}
