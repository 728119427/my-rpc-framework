package github.javaguide.testrpc;

import github.javaguide.config.RpcServiceConfig;
import github.javaguide.factory.SingletonFactory;
import github.javaguide.proxy.RpcClientProxy;
import github.javaguide.remoting.transport.netty.client.NettyRpcClient;

/**
 * @Description TODO
 * @Author zhengdongyuan@hanyangtech.cn
 * @Date 2022/10/9 10:37
 */
public class NettyClientMain {
    public static void main(String[] args) {
        NettyRpcClient nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder().version("1").group("hello").build();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(nettyRpcClient, rpcServiceConfig);
        HelloService helloService = (HelloService) rpcClientProxy.getProxy(HelloService.class);
        String res = helloService.sayHello("haha");
        System.out.println("========================================"+res);

    }
}
