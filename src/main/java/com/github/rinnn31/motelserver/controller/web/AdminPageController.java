package com.github.rinnn31.motelserver.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {
    @GetMapping("/admin/dashboard")
    public String usersPage() {
        return "forward:/dashboard.html";
    }
    
    @GetMapping("/admin/login")
    public String loginPage() {
        return "forward:/login.html";
    }

    @GetMapping("/admin/statistics")
    public String statisticsPage() {
        return "forward:/statistics.html";
    }

    @GetMapping("/admin/motels")
    public String motelsListPage() {
        return "forward:/motels-list.html";
    }

    @GetMapping("/admin/motel/{motelId}")
    public String motelDetailPage() {
        return "forward:/motel-info.html";
    }
}
