package github.javaguide.remoting.transport.netty.client;

import github.javaguide.remoting.dto.RpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UnprocessedRequests {

    private final Map<Long, CompletableFuture<RpcResponse<Object>>> futureMap = new ConcurrentHashMap<>();

    public void add(long requestId,CompletableFuture<RpcResponse<Object>> resultFuture){
        futureMap.put(requestId,resultFuture);
    }

    public void complete(RpcResponse<Object> response){
        CompletableFuture<RpcResponse<Object>> future = futureMap.remove(response.getRequestId());
        if(future!=null){
            future.complete(response);
        }else {
            throw new RuntimeException("requestId不存在!");
        }
    }

}
