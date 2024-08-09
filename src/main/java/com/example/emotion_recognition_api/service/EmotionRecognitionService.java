package com.example.emotion_recognition_api.service;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.EvaluatorUtil;
import org.jpmml.evaluator.LoadingModelEvaluatorBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.xml.bind.JAXBException;
import org.xml.sax.SAXException;

@Service
public class EmotionRecognitionService {

    private final Evaluator evaluator;

    public EmotionRecognitionService() throws IOException, ParserConfigurationException, SAXException, JAXBException {
        this.evaluator = loadPmmlModel();
    }

    private Evaluator loadPmmlModel() throws IOException, ParserConfigurationException, SAXException, JAXBException {
        String modelFolder = new ClassPathResource("src/main/resources/model/mlp_model_68acc.pmml").getPath();
        Path modelPath = Paths.get(modelFolder);
        URL url = modelPath.toFile().toURI().toURL();
        
        System.out.println("URL : " + url.toString());

        Evaluator evaluator = new LoadingModelEvaluatorBuilder()
                .load(modelPath.toFile())
                .build();

        // Perform self-testing
        evaluator.verify();

        System.out.println("Modelo terminou de carregar");
        return evaluator;
    }

    public Map<String, ?> detectEmotionFromBlendshapes(Map<String, Double> blendshapes) {
        try {
            Map<String, ?> results = predictEmotion(blendshapes);
            
            // Decoupling results from the JPMML-Evaluator runtime environment
            results = EvaluatorUtil.decodeAll(results);
            
            System.out.println("Raw results: " + results);
            return results;
        } catch (Exception e) {
            System.err.println("Erro ao prever emoção: " + e.getMessage());
            return Map.of("error", "Erro ao processar os blendshapes: " + e.getMessage());
        }
    }

    private Map<String, ?> predictEmotion(Map<String, Double> data) {
        return this.evaluator.evaluate(data);
    }
}