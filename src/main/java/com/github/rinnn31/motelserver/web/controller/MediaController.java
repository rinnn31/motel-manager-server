package com.github.rinnn31.motelserver.web.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.rinnn31.motelserver.service.external.ObjectStorageService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/media")
public class MediaController {
    private final ObjectStorageService storageService;

    public MediaController(ObjectStorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/**")
    public void redirect(HttpServletRequest request,
                        HttpServletResponse response) throws IOException {

        String path = request.getRequestURI();
        String prefix = "/media/";
        String key = path.substring(path.indexOf(prefix) + prefix.length());

        String url = storageService.getPublicUrl(key);
        response.sendRedirect(url);
    }
}