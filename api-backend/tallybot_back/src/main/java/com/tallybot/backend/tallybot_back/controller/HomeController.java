package com.tallybot.backend.tallybot_back.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app")
public class HomeController {

    @GetMapping
    public String checkHealth() {
        return "서버 정상 작동중";
    }
}
