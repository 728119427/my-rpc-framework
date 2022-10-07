package github.javaguide.registry.zk;

import github.javaguide.registry.ServiceRegistry;

import java.net.InetSocketAddress;

public class ZkServiceRegistryImpl implements ServiceRegistry {


    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        String path =CuratorUtils.ZK_REGISTER_ROOT_PATH+"/"+rpcServiceName+"/"+inetSocketAddress.toString();
        CuratorUtils.createPersistentNode(CuratorUtils.getZkClient(),path);

    }
}
