package com.neo.web;

import com.neo.service.RedisService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.neo.model.User;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
public class UserController {
    @Autowired
    private RedisService redisService;
    @RequestMapping("/getUser")
    public User getUser() {
        User user=new User("aa@126.com", "aa", "aa123456", "aa","123");
        this.redisService.setCacheObject("aa@126.com", user, 1000L, TimeUnit.SECONDS);
        System.out.println("若下面没出现“无缓存的时候调用”字样且能打印出数据表示测试成功");
        return user;
    }
    @RequestMapping("/uid")
    public String uid() {
        User user = this.redisService.getCacheObject("aa@126.com");
        return user.toString();
    }
}