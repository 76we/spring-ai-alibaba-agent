package com.agent.knowledge.controller;

import com.agent.knowledge.service.DocumentIngestService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final DocumentIngestService ingestService;

    public AdminController(DocumentIngestService ingestService) {
        this.ingestService = ingestService;
    }

    @PostMapping("/seed")
    public Map<String,Object> seed() throws IOException{
        return Map.of("chunk",ingestService.send());
    }
}
