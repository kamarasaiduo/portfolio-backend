package com.saiduokamara.portfolio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewController {

    @GetMapping("/verify-success")
    public String verifySuccess(@RequestParam(required = false) String message,
                                @RequestParam(required = false) String error,
                                Model model) {
        if (error != null) {
            model.addAttribute("error", error);
        } else {
            model.addAttribute("message", message != null ? message : "Email verified successfully!");
        }
        return "verify-success";
    }
}