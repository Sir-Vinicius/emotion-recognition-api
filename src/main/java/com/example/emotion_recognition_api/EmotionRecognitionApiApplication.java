package com.example.emotion_recognition_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.emotion_recognition_api.camera.CameraTest;

@SpringBootApplication
public class EmotionRecognitionApiApplication {

    public static void main(String[] args) {
        // Iniciar o servidor web
        // SpringApplication.run(EmotionRecognitionApiApplication.class, args);
        
        // Inicie a CameraTest
        CameraTest cameraTest = new CameraTest(null);
        cameraTest.start();
    }

    @Configuration
    public static class WebConfig implements WebMvcConfigurer {
        @Override
        public void addResourceHandlers(@SuppressWarnings("null") ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/static/**")
                    .addResourceLocations("classpath:/static/");
        }
    }
}