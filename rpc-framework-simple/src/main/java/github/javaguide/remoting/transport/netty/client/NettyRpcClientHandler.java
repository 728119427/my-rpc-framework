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
import github.javaguide.enums.CompressTypeEnum;
import github.javaguide.enums.SerializationTypeEnum;
import github.javaguide.factory.SingletonFactory;
import github.javaguide.remoting.constants.RpcConstants;
import github.javaguide.remoting.dto.RpcMessage;
import github.javaguide.remoting.dto.RpcResponse;
import github.javaguide.utils.SnowflakeUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.net.InetSocketAddress;

/**
 * @Description TODO
 * @Author zhengdongyuan@hanyangtech.cn
 * @Date 2022/10/8 10:02
 */
@Slf4j
public class NettyRpcClientHandler extends SimpleChannelInboundHandler<RpcMessage> {


    private final UnprocessedRequests unprocessedRequests;
    private final NettyRpcClient nettyRpcClient;

    public NettyRpcClientHandler(){
        unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }




    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage) throws Exception {
         byte messageType = rpcMessage.getMessageType();
         if(RpcConstants.HEARTBEAT_RESPONSE_TYPE == messageType){
             log.info("heartBeat response: [{}]",rpcMessage.getData());
         }else if(RpcConstants.RESPONSE_TYPE==messageType){
             RpcResponse<Object> rpcResponse= (RpcResponse<Object>) rpcMessage.getData();
             unprocessedRequests.complete(rpcResponse);
         }

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleState state = ((IdleStateEvent) evt).state();
            if(IdleState.WRITER_IDLE==state){
                //写空闲，发送心跳
                RpcMessage rpcMessage = RpcMessage.builder()
                        .requestId(SnowflakeUtil.nextId())
                        .messageType(RpcConstants.HEARTBEAT_REQUEST_TYPE)
                        .codec(SerializationTypeEnum.HESSIAN.getCode())
                        .compress(CompressTypeEnum.GZIP.getCode())
                        .build();
                ctx.writeAndFlush(rpcMessage).addListener(future -> {
                    if(future.isSuccess()){
                        log.info("send ping message to [{}]", ctx.channel().remoteAddress().toString());
                    }else {
                        log.error("send ping message to [{}] fail, msg:[{}]",((InetSocketAddress)ctx.channel().remoteAddress()).toString(),future.cause().getMessage());
                    }
                });
            }
        }else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
