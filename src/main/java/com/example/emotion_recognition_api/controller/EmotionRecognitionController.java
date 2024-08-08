package com.example.emotion_recognition_api.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.emotion_recognition_api.service.EmotionRecognitionService;

@RestController
public class EmotionRecognitionController {

    private static final Logger logger = LoggerFactory.getLogger(EmotionRecognitionController.class);
    private final EmotionRecognitionService emotionRecognitionService;

    @Autowired
    public EmotionRecognitionController(EmotionRecognitionService emotionRecognitionService) {
        this.emotionRecognitionService = emotionRecognitionService;
    }

    @PostMapping("/detectEmotionFromLandmarks")
    public ResponseEntity<Map<String, Object>> detectEmotion(@RequestBody List<Map<String, Double>> landmarks) {
        Map<String, Object> response = new HashMap<>();
        
        // Log para depuração
        logger.info("Recebido landmarks: " + landmarks);
        
        try {
            Map<String, Object> result = emotionRecognitionService.detectEmotionFromLandmarks(landmarks);
            response.put("emotion", result.get("emotion"));
            response.put("confidence", result.get("confidence"));
        } catch (Exception e) {
            logger.error("Erro ao processar os landmarks", e);
            response.put("error", "Erro ao processar os landmarks");
            return ResponseEntity.status(500).body(response);
        }

        return ResponseEntity.ok(response);
    }

}
