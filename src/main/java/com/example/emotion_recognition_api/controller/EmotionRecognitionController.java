package com.example.emotion_recognition_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.emotion_recognition_api.service.EmotionRecognitionService;

import java.util.Map;

@RestController
public class EmotionRecognitionController {

    private final EmotionRecognitionService emotionRecognitionService;

    @Autowired
    public EmotionRecognitionController(EmotionRecognitionService emotionRecognitionService) {
        this.emotionRecognitionService = emotionRecognitionService;
    }

    @PostMapping("/detectEmotionFromBlendshapes")
    public ResponseEntity<Map<String, ?>> predict(@RequestBody Map<String, Double> blendshapes) {
        try {
            Map<String, ?> results = emotionRecognitionService.detectEmotionFromBlendshapes(blendshapes);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao processar os blendshapes: " + e.getMessage()));
        }
    }
}