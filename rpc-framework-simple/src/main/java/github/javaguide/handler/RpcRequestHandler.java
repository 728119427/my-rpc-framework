package github.javaguide.handler;

import github.javaguide.extension.ExtensionLoader;
import github.javaguide.provider.ServiceProvider;
import github.javaguide.remoting.dto.RpcRequest;

import java.lang.reflect.Method;

/**
 * @Description rpc接口调用
 * @Author zhengdongyuan@hanyangtech.cn
 * @Date 2022/10/8 15:08
 */
public class RpcRequestHandler {
    private final ServiceProvider serviceProvider;

    public RpcRequestHandler(){
        serviceProvider = ExtensionLoader.getExtensionLoader(ServiceProvider.class).getExtension("zk");
    }

    public Object handle(RpcRequest rpcRequest){
        Object service = serviceProvider.getService(rpcRequest.getRpcServiceName());
        return doInvokeMethod(rpcRequest,service);
    }

    private Object doInvokeMethod(RpcRequest rpcRequest, Object service) {
        String interfaceName = rpcRequest.getInterfaceName();
        Class<?> interfaceClass = null;
        try {
            interfaceClass = Class.forName(interfaceName);
            Method method = interfaceClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            return method.invoke(service, rpcRequest.getParameters());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
