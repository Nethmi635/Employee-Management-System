package com.example.ems.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/_error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            
            if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
                return "redirect:/login-required";
            }
            if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return "error";
            }
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error";
            }
        }
        return "error";
    }
}
