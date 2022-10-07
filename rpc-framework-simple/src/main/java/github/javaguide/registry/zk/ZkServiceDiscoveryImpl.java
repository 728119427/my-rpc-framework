package github.javaguide.registry.zk;

import github.javaguide.enums.RpcErrorMessageEnum;
import github.javaguide.exception.RpcException;
import github.javaguide.extension.ExtensionLoader;
import github.javaguide.loadbalance.LoadBalance;
import github.javaguide.registry.ServiceDiscovery;
import github.javaguide.remoting.dto.RpcRequest;
import org.springframework.util.CollectionUtils;

import java.net.InetSocketAddress;
import java.util.List;

public class ZkServiceDiscoveryImpl implements ServiceDiscovery {

    private LoadBalance loadBalance;

    public ZkServiceDiscoveryImpl(){
        this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension("loadBalance");
    }

    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        List<String> childrenNodes = CuratorUtils.getChildrenNodes(CuratorUtils.getZkClient(), rpcServiceName);
        if(CollectionUtils.isEmpty(childrenNodes)){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND,rpcServiceName);
        }
        //负载均衡
        String selectedNode = loadBalance.selectServiceAddress(childrenNodes, rpcRequest);
        String ip = selectedNode.split(":")[0];
        String port = selectedNode.split(":")[1];
        return new InetSocketAddress(ip,Integer.parseInt(port));
    }
}
