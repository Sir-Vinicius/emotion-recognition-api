package com.example.emotion_recognition_api.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.emotion_recognition_api.service.EmotionRecognitionService;

@RestController
public class EmotionRecognitionController {

    private final EmotionRecognitionService emotionRecognitionService;

    @Autowired
    public EmotionRecognitionController(EmotionRecognitionService emotionRecognitionService) {
        this.emotionRecognitionService = emotionRecognitionService;
    }

    @PostMapping("/detectEmotionImage")
    public ResponseEntity<Map<String, Object>> detectEmotionImage(@RequestParam("image") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> result = emotionRecognitionService.detectEmotionFromImage(file);
            response.put("emotion", result.get("emotion"));
            response.put("confidence", result.get("confidence"));
            response.put("landmarks", result.get("landmarks"));
            response.put("imageWithLandmarks", result.get("imageWithLandmarks"));
        } catch (Exception e) {
            response.put("error", "Erro ao processar a imagem");
            return ResponseEntity.status(500).body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/detectEmotionVideo")
    public ResponseEntity<Map<String, Object>> detectEmotionVideo(@RequestParam("video") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> result = emotionRecognitionService.detectEmotionFromVideo(file);
            response.put("emotion", result.get("emotion"));
            response.put("confidence", result.get("confidence"));
            response.put("landmarks", result.get("landmarks"));
        } catch (Exception e) {
            response.put("error", "Erro ao processar o vídeo");
            return ResponseEntity.status(500).body(response);
        }

        return ResponseEntity.ok(response);
    }
}
