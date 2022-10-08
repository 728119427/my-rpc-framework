package github.javaguide.remoting.transport.netty.server;

import github.javaguide.config.CustomShutdownHook;
import github.javaguide.config.RpcServiceConfig;
import github.javaguide.extension.ExtensionLoader;
import github.javaguide.factory.SingletonFactory;
import github.javaguide.provider.ServiceProvider;
import github.javaguide.remoting.transport.netty.codec.RpcMessageDecoder;
import github.javaguide.remoting.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.xml.transform.sax.SAXTransformerFactory;
import java.util.concurrent.TimeUnit;

/**
 * @Description TODO
 * @Author zhengdongyuan@hanyangtech.cn
 * @Date 2022/10/8 18:03
 */
@Component
@Slf4j
public class NettyRpcServer {

    public static final int PORT = 8081;
    private final ServiceProvider serviceProvider ;

    public NettyRpcServer(){
        serviceProvider = ExtensionLoader.getExtensionLoader(ServiceProvider.class).getExtension("zk");
    }

    /**
     * 注册服务
     * @param rpcServiceConfig
     */
    public void registerService(RpcServiceConfig rpcServiceConfig){
        serviceProvider.publishService(rpcServiceConfig);
    }

    public void start(){
        CustomShutdownHook.getCustomShutdownHook().clearAll();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .option(ChannelOption.SO_BACKLOG,128)
                            .childHandler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch) throws Exception {
                                    ChannelPipeline p = ch.pipeline();
                                    p.addLast(new LoggingHandler(LogLevel.DEBUG));
                                    p.addLast(new RpcMessageEncoder());
                                    p.addLast(new RpcMessageDecoder());
                                    //心跳检测，30秒内没有接收到消息，触发读空闲，关闭客户端连接
                                    p.addLast(new IdleStateHandler(30,0,0, TimeUnit.SECONDS));
                                    p.addLast(new NettyRpcServerHandler());
                                }
                            });
            ChannelFuture channelFuture = serverBootstrap.bind(PORT).sync();
            channelFuture.channel().close().sync();
        } catch (Exception e) {
            log.error("start server error: {}",e.getMessage());
            throw new RuntimeException(e);
        }finally {
            //释放资源
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }


}
