package com.ccms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 首页控制器 - 处理SPA路由
 */
@Controller
public class HomeController {

    /**
     * 处理根路径访问
     */
    @GetMapping("/")
    public String home() {
        return "forward:/index.html";
    }

    /**
     * 处理前端SPA路由 - 所有未匹配的路径都重定向到index.html
     */
    @GetMapping("/**")
    public String redirect() {
        return "forward:/index.html";
    }
}