package github.javaguide.proxy;

import github.javaguide.config.RpcServiceConfig;
import github.javaguide.enums.RpcErrorMessageEnum;
import github.javaguide.enums.RpcResponseCodeEnum;
import github.javaguide.exception.RpcException;
import github.javaguide.remoting.dto.RpcRequest;
import github.javaguide.remoting.dto.RpcResponse;
import github.javaguide.remoting.transport.netty.client.NettyRpcClient;
import github.javaguide.utils.SnowflakeUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;

public class RpcClientProxy implements InvocationHandler {

    private static final String INTERFACE_NAME = "interfaceName";

    private NettyRpcClient nettyRpcClient;
    private RpcServiceConfig rpcServiceConfig;

    public RpcClientProxy(NettyRpcClient nettyRpcClient,RpcServiceConfig rpcServiceConfig){
        this.nettyRpcClient = nettyRpcClient;
        this.rpcServiceConfig=rpcServiceConfig;
    }


    /**
     * 获取接口代理对象
     * @param clazz
     * @return
     */
    public Object getProxy(Class clazz){
        return Proxy.newProxyInstance(clazz.getClassLoader(),new Class[]{clazz},this);
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcRequest = RpcRequest.builder()
                .requestId(SnowflakeUtil.nextId())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .methodName(method.getName())
                .interfaceName(method.getDeclaringClass().getName())
                .paramTypes(method.getParameterTypes())
                .parameters(args)
                .build();
        CompletableFuture<RpcResponse<Object>> resultFuture = (CompletableFuture<RpcResponse<Object>>) nettyRpcClient.sendRpcRequest(rpcRequest);
        RpcResponse<Object> rpcResponse = resultFuture.get();
        return rpcResponse.getData();
    }

    private void check(RpcResponse<Object> rpcResponse, RpcRequest rpcRequest) {
        if (rpcResponse == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        if (rpcRequest.getRequestId()!=(rpcResponse.getRequestId())) {
            throw new RpcException(RpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCodeEnum.SUCCESS.getCode())) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
    }
}
