package com.example.emotion_recognition_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// import com.example.emotion_recognition_api.camera.CameraTest;

@SpringBootApplication
public class EmotionRecognitionApiApplication {
    public static void main(String[] args) {
        // Iniciar o servidor web
        SpringApplication.run(EmotionRecognitionApiApplication.class, args);
        
        // Inicie a CameraTest
        // CameraTest cameraTest = new CameraTest();
        // cameraTest.start();
    }

    @EnableWebMvc
    @Configuration
    public static class WebConfig implements WebMvcConfigurer {
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            // Serve arquivos estáticos da pasta "static" diretamente
            registry.addResourceHandler("/**")
                    .addResourceLocations("classpath:/static/");
        }

        @Override
        public void addViewControllers(ViewControllerRegistry registry) {
            // Redireciona a raiz para o index.html
            registry.addViewController("/").setViewName("forward:/index.html");
        }
    }
}