package com.niehziliang.annotation.demo.aop;

import com.niehziliang.annotation.demo.annos.IgnoreToken;
import com.niehziliang.annotation.demo.controller.LoginController;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;

/**
 * @Author NieZhiLiang
 * @Email nzlsgg@163.com
 * @Date 2018/12/6 上午11:04
 */
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
