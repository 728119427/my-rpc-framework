package github.javaguide.remoting.transport.netty.server;

import github.javaguide.enums.RpcResponseCodeEnum;
import github.javaguide.factory.SingletonFactory;
import github.javaguide.handler.RpcRequestHandler;
import github.javaguide.remoting.constants.RpcConstants;
import github.javaguide.remoting.dto.RpcMessage;
import github.javaguide.remoting.dto.RpcRequest;
import github.javaguide.remoting.dto.RpcResponse;
import github.javaguide.remoting.transport.netty.client.NettyRpcClientHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @Description TODO
 * @Author zhengdongyuan@hanyangtech.cn
 * @Date 2022/10/8 15:34
 */
@Slf4j
public class NettyRpcServerHandler extends SimpleChannelInboundHandler<RpcMessage> {
    private final RpcRequestHandler rpcRequestHandler;

    public NettyRpcServerHandler(){
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) throws Exception {
        RpcMessage retMsg = RpcMessage.builder()
                .compress(msg.getCompress())
                .codec(msg.getCodec())
                .requestId(msg.getRequestId())
                .build();
        byte messageType = msg.getMessageType();
        if(RpcConstants.HEARTBEAT_REQUEST_TYPE==messageType){
            //心跳检测的消息
            retMsg.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
            retMsg.setData(RpcConstants.PONG);
        }else if(RpcConstants.REQUEST_TYPE==messageType){
            RpcRequest rpcRequest= (RpcRequest) msg.getData();
            try {
                Object res = rpcRequestHandler.handle(rpcRequest);
                RpcResponse<Object> rpcResponse = RpcResponse.success(res, rpcRequest.getRequestId());
                retMsg.setMessageType(RpcConstants.RESPONSE_TYPE);
                retMsg.setData(rpcResponse);
            } catch (Exception e) {
                log.error("rpc method invoke fail,rpcRequest:[{}]",rpcRequest);
                RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL,e.getMessage());
                retMsg.setMessageType(RpcConstants.RESPONSE_TYPE);
                retMsg.setData(rpcResponse);
            }
        }
        ctx.writeAndFlush(retMsg).addListener(future -> {
           if(!future.isSuccess()){
               log.error("send msg failed: [{}]",future.cause().getMessage());
           }
        });
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleState state = ((IdleStateEvent) evt).state();
            if(IdleState.READER_IDLE==state){
                log.info("heartBeat check fail,close channel:[{}]",((InetSocketAddress)ctx.channel().localAddress()).toString());
            }
        }else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
