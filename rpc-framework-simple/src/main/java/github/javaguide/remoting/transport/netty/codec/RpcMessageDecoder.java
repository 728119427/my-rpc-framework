package github.javaguide.remoting.transport.netty.codec;

import github.javaguide.compress.Compress;
import github.javaguide.enums.CompressTypeEnum;
import github.javaguide.enums.SerializationTypeEnum;
import github.javaguide.extension.ExtensionLoader;
import github.javaguide.remoting.constants.RpcConstants;
import github.javaguide.remoting.dto.RpcMessage;
import github.javaguide.remoting.dto.RpcRequest;
import github.javaguide.remoting.dto.RpcResponse;
import github.javaguide.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteOrder;
import java.util.List;
/**
 * <p>
 * custom protocol decoder
 * <p>
 * <pre>
 *   0     1     2     3     4        5    6    7    8      9          10      11       12    13     14    15    16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+------+-----+
 *   |   magic   code        |version |Message Body length | messageType| codec|compress|                         |
 *   +-----------------------------------------------------------------------------------+-----+-----+------+-----+
 *   |      RequestId        |                              padding                                               |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+-----------------+
 *   |                                         body                                                               |
 *   |                                                                                                            |
 *   |                                        ... ...                                                             |
 *   +------------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 * body（object类型数据）
 * </pre>
 */
@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {

    public RpcMessageDecoder(){
        this(RpcConstants.MAX_FRAME_LENGTH,5,4,23,0);
    }

    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object frame = super.decode(ctx, in);
        if(frame instanceof ByteBuf){
            ByteBuf fullFrame = (ByteBuf) frame;
            return decodeFullFrame(fullFrame);
        }
        return frame;
    }

    /**
     * 从完整的帧中获取消息体
     * @param in
     * @return
     */
    private Object decodeFullFrame(ByteBuf in){
        //读取魔数
        byte[] magic = new byte[4];
        in.readBytes(magic);
        //读取版本
        byte version = in.readByte();
        //读取消息长度
        int bodyLength = in.readInt();
        //读取消息类型
        byte messageType = in.readByte();
        //codec
        byte codec = in.readByte();
        //compress
        byte compress = in.readByte();
        //requestId
        long requestId = in.readLong();
        //构建返回消息
        RpcMessage rpcMessage = RpcMessage.builder()
                .messageType(messageType)
                .codec(codec)
                .compress(compress)
                .requestId(requestId)
                .build();
        //================心跳消息===============================
        if(RpcConstants.HEARTBEAT_REQUEST_TYPE==messageType){
            rpcMessage.setData(RpcConstants.PING);
            return rpcMessage;
        }
        if(RpcConstants.HEARTBEAT_RESPONSE_TYPE==messageType){
            rpcMessage.setData(RpcConstants.PONG);
            return rpcMessage;
        }
        //===============rpc请求响应消息=====================
        //字节填充
        byte[] padding = new byte[12];
        in.readBytes(padding);
        //读取消息体
        byte[] body = new byte[bodyLength];
        in.readBytes(body);
        //解压缩，反序列化
        Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(SerializationTypeEnum.getName(codec));
        Compress compressImpl = ExtensionLoader.getExtensionLoader(Compress.class).getExtension(CompressTypeEnum.getName(compress));
        byte[] decompress = compressImpl.decompress(body);
        if(RpcConstants.REQUEST_TYPE==messageType){
            RpcRequest rpcRequest = serializer.deserialize(decompress, RpcRequest.class);
            rpcMessage.setData(rpcRequest);
            return rpcMessage;
        }
        RpcResponse<Object> rpcResponse = serializer.deserialize(decompress, RpcResponse.class);
        rpcMessage.setData(rpcResponse);
        log.info("length={},messageType={},codec={},compress={},requestId={},body={}",bodyLength,messageType,rpcMessage.getCodec(),rpcMessage.getCompress(),rpcMessage.getRequestId(),rpcMessage.getData());
        return rpcMessage;
    }
}
