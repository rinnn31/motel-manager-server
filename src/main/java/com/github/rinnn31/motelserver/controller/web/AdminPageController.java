package com.github.rinnn31.motelserver.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminPageController {
    @GetMapping("/admin/users")
    public String usersPage() {
        return "forward:/users.html";
    }
    
    @GetMapping("/admin/login")
    public String loginPage() {
        return "forward:/login.html";
    }

    @GetMapping("/admin/motels")
    public String motelsListPage(@RequestParam(required = false) String landlordId) {
        if (landlordId != null) {
            return "forward:/landlord-motels.html?landlordId=" + landlordId;
        } else {
            return "forward:/motels.html";
        }
    }

    @GetMapping("/admin/motel/{motelId}")
    public String motelDetailPage() {
        return "forward:/motel-info.html";
    }
}
