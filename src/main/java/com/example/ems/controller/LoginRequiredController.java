package com.example.ems.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginRequiredController {

    @GetMapping("/login-required")
    public String loginRequired() {
        return "login-required";
    }
}
