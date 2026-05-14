package com.ccms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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
     * 处理常见的前端路径
     */
    @GetMapping({"/login", "/dashboard", "/expense", "/budget", "/approval", "/report"})
    public String spaRoutes() {
        return "forward:/index.html";
    }
}