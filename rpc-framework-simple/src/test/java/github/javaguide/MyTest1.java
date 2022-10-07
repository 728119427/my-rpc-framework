package github.javaguide;

import github.javaguide.extension.Holder;
import github.javaguide.factory.SingletonFactory;
import github.javaguide.remoting.constants.RpcConstants;
import github.javaguide.remoting.dto.RpcMessage;
import github.javaguide.remoting.dto.RpcResponse;
import github.javaguide.remoting.transport.netty.codec.RpcMessageDecoder;
import github.javaguide.remoting.transport.netty.codec.RpcMessageEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MyTest1 {

    @Test
    public void test1(){
        Map<String,Object> map = new ConcurrentHashMap<>();
        for (int i = 0; i < 20; i++) {
            new Thread(()->{
                map.computeIfAbsent("aa",k->{
                    System.out.println("success");
                    return new Object();
                });
            }).start();
        }
    }


    @Test
    public void test2() throws Exception {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new LoggingHandler(), new RpcMessageEncoder(), new RpcMessageDecoder());
        RpcResponse<Object> response = RpcResponse.builder().message("haha").code(11).requestId(123L).build();
        RpcMessage rpcMessage = RpcMessage.builder().messageType(RpcConstants.RESPONSE_TYPE).data(response).compress((byte) 1).codec((byte) 3).requestId(123L).build();
        RpcMessage rpcMessage1= RpcMessage.builder().messageType(RpcConstants.HEARTBEAT_REQUEST_TYPE).data(RpcConstants.PING).compress((byte) 1).codec((byte) 3).requestId(123L).build();
        //encode
        embeddedChannel.writeOutbound(rpcMessage);

        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        new RpcMessageEncoder().encode(null,rpcMessage,buffer);

        //decode
        embeddedChannel.writeInbound(buffer);
    }
}
