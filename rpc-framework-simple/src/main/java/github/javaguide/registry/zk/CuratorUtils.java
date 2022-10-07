package github.javaguide.registry.zk;

import github.javaguide.utils.CollectionUtil;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
public class CuratorUtils {

    private CuratorUtils(){}

    private static final String DEFAULT_ZK_ADDRESS = "127.0.0.1:2181";
    public static final String ZK_REGISTER_ROOT_PATH = "/my-rpc";
    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();
    private static CuratorFramework zkClient;

    public static void createPersistentNode(CuratorFramework zkClient, String path){
        try {
            if(REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path)!=null){
                log.info("node already exists,node is [{}]",path);
                return;
            }
            zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
            REGISTERED_PATH_SET.add(path);
            log.info("node create successfully,node is [{}]",path);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName){
        if(SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)){
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }
        String path = ZK_REGISTER_ROOT_PATH + "/"+rpcServiceName;
        try {
            List<String> childrenNodes = zkClient.getChildren().forPath(path);
            if(!CollectionUtil.isEmpty(childrenNodes)){
                SERVICE_ADDRESS_MAP.put(rpcServiceName,childrenNodes);
                //监听该服务节点的变化
                registerWatcher(rpcServiceName,zkClient);
            }
            return childrenNodes;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress){
        REGISTERED_PATH_SET.stream().forEach(path->{
            if(path.endsWith(inetSocketAddress.toString())){
                try {
                    zkClient.delete().forPath(path);
                } catch (Exception e) {
                    log.error("clear registry for path [{}] fail",path);
                }
            }
        });
        log.info(" All registered services for this provider [{}] are clear",inetSocketAddress);
    }

    public static CuratorFramework getZkClient(){
        try {
            if(zkClient!=null && zkClient.getState()== CuratorFrameworkState.STARTED){
                return zkClient;
            }
            ExponentialBackoffRetry exponentialBackoffRetry = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
            zkClient = CuratorFrameworkFactory.builder()
                        .connectString(DEFAULT_ZK_ADDRESS)
                        .retryPolicy(exponentialBackoffRetry)
                        .connectionTimeoutMs(30000)
                        .build();
            zkClient.start();
            return zkClient;
        } catch (Exception e) {
            log.error("zk connect timeout : {}",e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void registerWatcher(String rpcServiceName, CuratorFramework zkClient){
        String watchPath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, watchPath, true);
        PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework,pathChildrenCacheEvent)->{
            List<String> newChildrenNodes = curatorFramework.getChildren().forPath(watchPath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName,newChildrenNodes);
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        try {
            pathChildrenCache.start();
        } catch (Exception e) {
            log.error("pathChildrenCache start fail : {}",e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
