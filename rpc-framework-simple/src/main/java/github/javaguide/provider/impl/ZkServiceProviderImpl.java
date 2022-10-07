package github.javaguide.provider.impl;

import github.javaguide.config.RpcServiceConfig;
import github.javaguide.enums.RpcErrorMessageEnum;
import github.javaguide.exception.RpcException;
import github.javaguide.extension.ExtensionLoader;
import github.javaguide.provider.ServiceProvider;
import github.javaguide.registry.ServiceRegistry;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class ZkServiceProviderImpl implements ServiceProvider {

    private final Map<String, Object> serviceMap;
    private final Set<String> registeredService;
    private final ServiceRegistry serviceRegistry;

    public ZkServiceProviderImpl(){
        this.serviceMap = new ConcurrentHashMap<>();
        this.registeredService = new CopyOnWriteArraySet<>();
        serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("zk");
    }


    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        if(registeredService.contains(rpcServiceName)){
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName,rpcServiceConfig.getService());
    }

    @Override
    public Object getService(String rpcServiceName) {
        Object o = serviceMap.get(rpcServiceName);
        if(o==null){
            throw  new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return o;
    }

    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        addService(rpcServiceConfig);
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            serviceRegistry.registerService(rpcServiceName,new InetSocketAddress(ip,8081));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
