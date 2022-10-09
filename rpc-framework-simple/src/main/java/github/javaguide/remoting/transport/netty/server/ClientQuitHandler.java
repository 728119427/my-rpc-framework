package github.javaguide.remoting.transport.netty.server;

import github.javaguide.factory.SingletonFactory;
import github.javaguide.remoting.transport.netty.client.ChannelProvider;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @Description TODO
 * @Author zhengdongyuan@hanyangtech.cn
 * @Date 2022/10/9 11:52
 */
@Slf4j
public class ClientQuitHandler extends ChannelInboundHandlerAdapter {

    private final ChannelProvider channelProvider = SingletonFactory.getInstance(ChannelProvider.class);


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress socketAddress= (InetSocketAddress) ctx.channel().localAddress();
        log.info("client has closed :[{}]",socketAddress.toString());
        channelProvider.remove(socketAddress);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(cause.getMessage()!=null && cause.getMessage().contains("强迫关闭")){
            InetSocketAddress socketAddress= (InetSocketAddress) ctx.channel().localAddress();
            log.info("client close exceptionally! client:[{}],msg:[{}]",socketAddress.toString(),cause.getMessage());
        }else {
            super.exceptionCaught(ctx,cause);
        }

    }
}
