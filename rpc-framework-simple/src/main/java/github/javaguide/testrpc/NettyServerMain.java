package github.javaguide.testrpc;

import github.javaguide.annotation.RpcScan;
import github.javaguide.config.RpcServiceConfig;
import github.javaguide.remoting.transport.netty.codec.RpcMessageDecoder;
import github.javaguide.remoting.transport.netty.codec.RpcMessageEncoder;
import github.javaguide.remoting.transport.netty.server.NettyRpcServer;
import github.javaguide.remoting.transport.netty.server.NettyRpcServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 * @Description TODO
 * @Author zhengdongyuan@hanyangtech.cn
 * @Date 2022/10/9 10:37
 */
@Slf4j
public class NettyServerMain {
    public static final int PORT = 8081;

    public static void main(String[] args) throws IOException {
        NettyRpcServer nettyRpcServer = new NettyRpcServer();
        HelloService helloService = new HelloServiceImpl();
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig("1", "hello", helloService);
        nettyRpcServer.registerService(rpcServiceConfig);
        nettyRpcServer.start();

    }
}
