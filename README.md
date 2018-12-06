 说到注解我们平常用的可以说非常多啦，说几个常用的的注解 `@RestController` `@Service` `@Autowired`
 这些都是我们平常使用spring框架最常见的注解了，我们只知道它们非常好用，使用`@RestController` 就能构建一个restful的控制器,`@Service` 这个是我们常用的mvc架构中的业务层使用的注解，将类交给spring容器管理，我们要用的话直接使用`@Autowired`就能将类自动注入。我们都知道用了这些注解非常的方便，今天我们自己也来写一个自己的注解。

### 需求
一个项目，有些方法是需要被保护起来的，有写方法是开放出来不需要保护的，比如登录 注册等 都不需要保护，解决方案有很多，今天我们就使用springboot的aop  和自定义注解来解决这个需求。
在创建自己的注解之前，了解一些注解的知识。
 1.首先java `jdk`给我们提供了meta-annotation用于自定义注解的时候使用，这四个注解为：@Target，@Retention，@Documented 和@Inherited。
-  第一个注解`@Target`
`@Target`:用于描述注解的使用范围（即：被描述的注解可以用在什么地方），其源码如下：
```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Target {
    ElementType[] value();
}
```
 我们可以看到它只有一个属性值，是个枚举类型的数组，我们进该枚举类型看看
 ```java
 public enum ElementType {
    TYPE,//用于描述类、接口(包括注解类型) 或enum声明
    FIELD, //用于描述成员变量；
    METHOD,//用于描述方法
    PARAMETER,//用于描述参数
    CONSTRUCTOR,//用于描述构造器
    LOCAL_VARIABLE,//用于描述局部变量；
    ANNOTATION_TYPE,//注解类型声明 该注解可用于描述注解
    PACKAGE,//用于描述包
    TYPE_PARAMETER,//这个是jdk1.8后加入的类型  表示这个 Annotation 可以用在 Type 的声明式前
    TYPE_USE//表示这个 Annotation 可以用在所有使用 Type 的地方（如：泛型，类型转换等）
}
 ```
- 第二个注解`@Retention`
`@Retention`:指定被描述的注解在什么范围内有效。源码如下：
```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Retention {
    RetentionPolicy value();
}
```
可以看到，该注解也只有一个枚举的属性，我们进该枚举类型看看
```java
public enum RetentionPolicy {
    SOURCE,//表示描述程序编译时
    CLASS,//在class文件中有效（即class保留
    RUNTIME//在运行时有效（即运行时保留）
}
```
- 第三个注解：`@Documented`

`@Documented` 是一个标记注解，木有成员，用于描述其它类型的annotation应该被作为被标注的程序成员的公共API，因此可以被例如javadoc此类的工具文档化。
```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Documented {
}
```
- 第四个注解 `@Inherited`

 `@Inherited` 元注解是一个标记注解，@Inherited阐述了某个被标注的类型是被继承的。如果一个使用了@Inherited修饰的annotation类型被用于一个class，则这个annotation将被用于该class的子类。

```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Retention {
    RetentionPolicy value();
}
```

### 下面我们开始编写我们自己的注解
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreToken {
}
```
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Log {
    String name() default "";
}
```

编写aop拦截所有的controller请求
```java
@Component
@Aspect
@Order(1)
public class TokenAspect {
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;
    @Autowired
    private LoginController loginController;

    @Pointcut("within(com.niehziliang.annotation.demo.controller..*)")
    public void checkToken () {}

    @Before("checkToken()")
    public void checkToken (JoinPoint joinPoint) throws IOException {

        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        //获取当前访问的类方法
        Method targetMethod = signature.getMethod();
        //判断是否是注解修饰的类，如果是则不需要校验token
        if(!targetMethod.isAnnotationPresent(IgnoreToken.class)){
            String token = request.getParameter("token");
            if (null == token || "".equals(token)) {
                response.setCharacterEncoding("utf-8");
                response.setContentType("application/json; charset=utf-8");
                PrintWriter out = response.getWriter();
                out.print("token不能为空");
                out.flush();
                out.close();
            } else {
                if (!loginController.chkToken(token)) {
                    response.setCharacterEncoding("utf-8");
                    response.setContentType("application/json; charset=utf-8");
                    PrintWriter out = response.getWriter();
                    out.print("token不合法");
                    out.flush();
                    out.close();
                }
            }
        }
    }

}
```
aop日志拦截
```java
@Component
@Aspect
@Order(2)
public class LogAspect {
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private LoginController loginController;

    @Pointcut("@annotation(com.niehziliang.annotation.demo.annos.Log)")
    public void saveLog() {}

    @Around("saveLog()")
    public Object saveLog(ProceedingJoinPoint point) throws Throwable {
        long start = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Log logAnnotation = method.getAnnotation(Log.class);
        String name = null;
        if (logAnnotation != null) {
            // 注解上的描述
            name = logAnnotation.name();
        }
        // 请求的方法名
        String className = point.getTarget().getClass().getName();
        String methodName = signature.getName();
        // 请求的方法参数值
        Object[] args = point.getArgs();
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        String[] paramNames = u.getParameterNames(method);
        String params = "";
        if (args != null && paramNames != null) {
            for (int i = 0; i < args.length; i++) {
                params += "  " + paramNames[i] + ": " + args[i];
            }
        }
        String ip = IpUtils.getIpAddress(request);
        long time = System.currentTimeMillis() - start;
        StringBuffer log = new StringBuffer();
        log.append("注解上的name:").append(name).append("=======")
                .append("请求的方法:").append(className).append(".").append(methodName).append("=====")
                .append("请求参数:").append(params).append("=======")
                .append("请求的ip:").append(ip).append("耗时:").append(time).append("ms");
        System.out.println(log.toString());
        loginController.saveLog(log.toString());
        return point.proceed();
    }
}

```

编写controller代码, 登录方法加了我自己定义的注解`@IgnoreToken` aop在拦截的时候判断有我们自己定义注解就不会去校验token啦，获取密码的方法则会进行token的校验
```java
@RestController
public class LoginController {
    public static Map<String,String> map = new HashMap<>();
    public static List<String> logList = new ArrayList<>();
    public static Set<String> tokenSet = new HashSet<>();

    @RequestMapping(value = "login")
    @IgnoreToken
    @Log
    public String login(String userName,String password) {
        map.put(userName,password);
        //保存token
        tokenSet.add(userName+password);
        //返回token
        return userName+password;
    }

    @RequestMapping(value = "query")
    @Log(name = "获取密码")
    public String getPassword(String userName) {
        //获取用户密码
        return map.get(userName);
    }

    @RequestMapping(value = "logs")
    @Log(name = "获取日志信息")
    public String getLogMap() {
        return JSON.toJSONString(logList);
    }
 }
```
下面我在浏览器输入请求登录地址进行登录
```js
// 登录获取token
http://127.0.0.1:8080/login?userName=admin&password=adminadmin
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20181206151500409.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM4MDgyMzA0,size_16,color_FFFFFF,t_70)

不带token访问受保护的方法
```js
// 获取用户密码
http://127.0.0.1:8080/query?userName=admin
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20181206151649729.png)

带正确的token访问受保护的犯法
```js
// 获取用户密码
http://127.0.0.1:8080/query?userName=admin&token=adminadminadmin
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20181206151745269.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM4MDgyMzA0,size_16,color_FFFFFF,t_70)

获取用户访问日志信息
![在这里插入图片描述](https://img-blog.csdnimg.cn/20181206151831679.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM4MDgyMzA0,size_16,color_FFFFFF,t_70)

项目地址：https://github.com/niezhiliang/annotation-demo