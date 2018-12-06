package com.niehziliang.annotation.demo.controller;

import com.alibaba.fastjson.JSON;
import com.niehziliang.annotation.demo.annos.IgnoreToken;
import com.niehziliang.annotation.demo.annos.Log;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @Author NieZhiLiang
 * @Email nzlsgg@163.com
 * @Date 2018/12/6 上午11:05
 */
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


    @IgnoreToken//这里使用这个注解是偷懒，因为aop对controller包下所有方法都进行了token拦截，为了避免token验证
    public boolean chkToken(String token) {
        return tokenSet.contains(token);
    }

    @IgnoreToken//同上
    public void saveLog(String log) {
        logList.add(log);
    }
}
