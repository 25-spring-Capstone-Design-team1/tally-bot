package com.tallybot.backend.tallybot_back.controller;

import com.tallybot.backend.tallybot_back.domain.Calculate;
import com.tallybot.backend.tallybot_back.repository.CalculateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevController {

    private final CalculateRepository calculateRepository;

    @GetMapping("/calculates")
    public List<Calculate> getAllCalculates() {
        return calculateRepository.findAll();
    }
}
