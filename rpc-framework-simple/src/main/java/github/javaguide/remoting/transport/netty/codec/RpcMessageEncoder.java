package github.javaguide.remoting.transport.netty.codec;

import github.javaguide.compress.Compress;
import github.javaguide.enums.CompressTypeEnum;
import github.javaguide.enums.SerializationTypeEnum;
import github.javaguide.extension.ExtensionLoader;
import github.javaguide.remoting.constants.RpcConstants;
import github.javaguide.remoting.dto.RpcMessage;
import github.javaguide.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.sql.PseudoColumnUsage;

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
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {

    @Override
    public void encode(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage, ByteBuf out) throws Exception {
        int bodyLength=0;
        byte[] body = null;
        byte messageType = rpcMessage.getMessageType();
        if(RpcConstants.HEARTBEAT_REQUEST_TYPE!=messageType && RpcConstants.HEARTBEAT_RESPONSE_TYPE!=messageType){
            //先获取消息体并序列化
            Object msg = rpcMessage.getData();
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(SerializationTypeEnum.getName(rpcMessage.getCodec()));
            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class).getExtension(CompressTypeEnum.getName(rpcMessage.getCompress()));
            byte[] serializeBody = serializer.serialize(msg);
            body = compress.compress(serializeBody);
            bodyLength = body.length;
        }
        //魔数
        out.writeBytes(RpcConstants.MAGIC_NUMBER);
        //协议版本
        out.writeByte(RpcConstants.VERSION);
        //消息体长度
        out.writeInt(bodyLength);
        //消息类型
        out.writeByte(messageType);
        //序列化方式
        out.writeByte(rpcMessage.getCodec());
        //压缩方式
        out.writeByte(rpcMessage.getCompress());
        //requestId
        out.writeLong(rpcMessage.getRequestId());
        //字节填充
        byte[] padding = new byte[12];
        out.writeBytes(padding);
        //消息体
        if(body!=null){
            out.writeBytes(body);
        }
        log.info("length={},messageType={},codec={},compress={},requestId={},body={}",bodyLength,messageType,rpcMessage.getCodec(),rpcMessage.getCompress(),rpcMessage.getRequestId(),rpcMessage.getData());
    }
}
