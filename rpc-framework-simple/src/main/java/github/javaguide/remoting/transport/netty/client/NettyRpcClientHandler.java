package github.javaguide.remoting.transport.netty.client;

import github.javaguide.factory.SingletonFactory;
import github.javaguide.remoting.constants.RpcConstants;
import github.javaguide.remoting.dto.RpcMessage;
import github.javaguide.remoting.dto.RpcRequest;
import github.javaguide.remoting.dto.RpcResponse;
import github.javaguide.remoting.transport.RpcRequestTransport;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyRpcClientHandler extends SimpleChannelInboundHandler<RpcMessage> {
    private final UnprocessedRequests unprocessedRequests;
    private final NettyRpcClient nettyRpcClient;

    public NettyRpcClientHandler(){
        unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) throws Exception {
        byte messageType = msg.getMessageType();
        if(RpcConstants.HEARTBEAT_RESPONSE_TYPE==messageType){
            log.info("heartBeat response [{}]",msg.getData());
        }else if(RpcConstants.RESPONSE_TYPE==messageType){
            RpcResponse<Object> rpcResponse= (RpcResponse<Object>) msg.getData();
            unprocessedRequests.complete(rpcResponse);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
