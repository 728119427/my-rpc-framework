package github.javaguide.remoting.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageTypeEnum {

    RPC_REQUEST_MSG((byte) 1,RpcRequest.class),
    RPC_RESPONSE_MSG((byte) 2,RpcResponse.class),
    HEARTBEAT_REQUEST_MSG((byte) 3,String.class),
    HEARTBEAT_RESPONSE_MSG((byte) 4,String.class);

    private final byte messageType;
    private final Class clazz;

    public Class getTypeClass(byte code){
        for (MessageTypeEnum messageTypeEnum : MessageTypeEnum.values()) {
            if(messageTypeEnum.getMessageType()==code){
                return messageTypeEnum.clazz;
            }
        }
        return null;
    }

}
