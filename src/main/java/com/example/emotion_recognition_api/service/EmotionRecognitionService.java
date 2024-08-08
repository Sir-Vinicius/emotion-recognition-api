package com.example.emotion_recognition_api.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.dmg.pmml.Model;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.EvaluatorUtil;
import org.jpmml.evaluator.ModelEvaluator;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.model.PMMLUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.xml.bind.JAXBException;
import org.xml.sax.SAXException;

@Service
public class EmotionRecognitionService {

    private static final String ERROR_PROCESSING_IMAGE = "Erro ao processar a imagem";
    private final ModelEvaluator<?> evaluator;

    public EmotionRecognitionService() throws ParserConfigurationException {
        try {
            this.evaluator = loadPmmlModel();
        } catch (IOException | SAXException | JAXBException e) {
            String errorMessage = "Falha ao carregar o modelo PMML: " + e.getMessage();
            throw new RuntimeException(errorMessage);
        }
    }

    private ModelEvaluator<?> loadPmmlModel() throws IOException, SAXException, JAXBException, ParserConfigurationException {
        try (InputStream is = new ClassPathResource("emotion_recognition_model.pmml").getInputStream()) {
            PMML pmml = PMMLUtil.unmarshal(is);
            System.out.println("Loaded PMML model");
            Model model = pmml.getModels().get(0);
            System.out.println("Loaded model: " + model.getModelName());
            ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
            return modelEvaluatorFactory.newModelEvaluator(pmml, model);
        }
    }    

    public Map<String, Object> detectEmotionFromLandmarks(List<Map<String, Double>> landmarks) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Double> input = prepareInput(landmarks);
            System.out.println("Input for prediction: " + input); // Adicione esta linha
            Map<String, Object> prediction = predictEmotion(input);
            System.out.println("Prediction result: " + prediction); // Adicione esta linha
    
            double predictedEmotion = (Double) prediction.getOrDefault("predictedEmotion", -1.0);
            double confidence = (Double) prediction.getOrDefault("confidence", 0.0);
    
            response.put("emotion", predictedEmotion);
            response.put("confidence", confidence);
        } catch (Exception e) {
            response.put("error", ERROR_PROCESSING_IMAGE + ": " + e.getMessage());
        }
        return response;
    }
    

    private Map<String, Double> prepareInput(List<Map<String, Double>> landmarks) {
        Map<String, Double> input = new HashMap<>();
        for (int i = 0; i < landmarks.size(); i++) {
            Map<String, Double> point = landmarks.get(i);
            input.put("x" + i, point.get("x"));
            input.put("y" + i, point.get("y"));
        }
        return input;
    }

    private Map<String, Object> predictEmotion(Map<String, Double> input) {
        Map<String, Object> result = new HashMap<>();
        try {
            System.out.println("Input for prediction: " + input); // Adicione esta linha
            Map<String, ?> results = evaluator.evaluate(input);
            results = EvaluatorUtil.decodeAll(results);
            System.out.println("Raw results: " + results); // Adicione esta linha
     
            for (Map.Entry<String, ?> entry : results.entrySet()) {
                String key = entry.getKey();
                Object resultValue = entry.getValue();
                result.put(key, resultValue);
            }
        } catch (Exception e) {
            System.err.println("Erro ao prever emoção: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
}    