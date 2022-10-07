package github.javaguide.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonFactory {

    private static final Map<String,Object> SINGLETON_FACTORY = new ConcurrentHashMap<>();

    private SingletonFactory(){}


    public static  <T> T getInstance(Class<T> clazz){
        if(clazz==null){
            throw new IllegalArgumentException();
        }
        String key = clazz.getCanonicalName();
        if(SINGLETON_FACTORY.containsKey(key)){
            return clazz.cast(SINGLETON_FACTORY.get(key));
        }else {
            return clazz.cast(SINGLETON_FACTORY.computeIfAbsent(key,k->{
                try {
                    return clazz.getDeclaredConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
    }

}
