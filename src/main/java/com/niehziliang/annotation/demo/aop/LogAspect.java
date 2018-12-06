package com.niehziliang.annotation.demo.aop;

import com.niehziliang.annotation.demo.annos.Log;
import com.niehziliang.annotation.demo.controller.LoginController;
import com.niezhiliang.common.utils.ip.IpUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * @Author NieZhiLiang
 * @Email nzlsgg@163.com
 * @Date 2018/12/6 下午2:49
 */
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
