package github.javaguide.testrpc;

/**
 * @Description TODO
 * @Author zhengdongyuan@hanyangtech.cn
 * @Date 2022/10/9 10:36
 */
public class HelloServiceImpl implements HelloService{
    @Override
    public String sayHello(String msg) {
        return "hello!";
    }
}
