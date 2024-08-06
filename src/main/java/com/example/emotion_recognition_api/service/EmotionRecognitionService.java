package com.example.emotion_recognition_api.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.dmg.pmml.Model;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.Computable;
import org.jpmml.evaluator.EvaluatorUtil;
import org.jpmml.evaluator.ModelEvaluator;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.model.PMMLUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import jakarta.xml.bind.JAXBException;

@Service
public class EmotionRecognitionService {

    private static final String ERROR_PROCESSING_IMAGE = "Erro ao processar a imagem";
    private final ModelEvaluator<?> evaluator;

    // Lista de linhas para desenhar pontos e linhas
    private static final int[][] listLines = {
        {130, 27}, {130, 23}, {27, 133}, {23, 133}, // olho esquerdo 
        {359, 257}, {359, 253}, {257, 362}, {253, 362}, // olho direito 
        {130, 70}, {107, 168}, {168, 336}, {300, 359}, // retas sobrancelhas olhos e nariz
        {168, 4}, // nariz 
        {133, 4}, {362, 4}, // olhos - ponta nariz
        {4, 50}, {4, 280}, // ponta nariz - bochecha
        {4, 0}, // ponta nariz - topo boca
        {70, 105}, {105, 107}, {107, 108}, {108, 104}, {104, 71}, {71, 70}, // sobrancelha esquerda 
        {336, 334}, {334, 300}, {300, 301}, {301, 333}, {333, 337}, {337, 336}, // sobrancelha direita
        {61, 40}, {61, 91}, {40, 0}, {91, 17}, {0, 270}, {17, 321}, {270, 291}, {321, 291}, // boca contorno
        {50, 61}, {280, 291}, // bochechas - boca
        {212, 216}, {216, 214}, {214, 207}, {207, 192}, {192, 197}, // lado boca esquerda zig zag 
        {432, 436}, {436, 434}, {434, 427}, {427, 416}, {416, 411}, // lado boca direita zig zag 
        {61, 212}, {291, 432}
    };

    public EmotionRecognitionService() {
        try {
            this.evaluator = loadPmmlModel();
        } catch (IOException | SAXException | ParserConfigurationException | JAXBException e) {
            String errorMessage = "Falha ao carregar o modelo PMML: " + e.getMessage();
            throw new RuntimeException(errorMessage);
        }
    }

    private ModelEvaluator<?> loadPmmlModel() throws IOException, SAXException, ParserConfigurationException, JAXBException {
        try (InputStream is = new ClassPathResource("emotion_recognition_model.pmml").getInputStream()) {
            PMML pmml = PMMLUtil.unmarshal(is);
            Model model = pmml.getModels().get(0);
            ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
            return modelEvaluatorFactory.newModelEvaluator(pmml, model);
        }
    }

    public Map<String, Object> detectEmotionFromLandmarks(List<Map<String, Double>> landmarks) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Double> input = prepareInput(landmarks);
            Map<String, Object> prediction = predictEmotion(input);

            double predictedEmotion = (Double) prediction.get("predictedEmotion");
            double confidence = (Double) prediction.get("confidence");

            response.put("emotion", mapEmotionToString(predictedEmotion));
            response.put("confidence", confidence);
        } catch (Exception e) {
            response.put("error", ERROR_PROCESSING_IMAGE);
        }
        return response;
    }

    private Map<String, Double> prepareInput(List<Map<String, Double>> landmarks) {
        Map<String, Double> input = new HashMap<>();
        for (int i = 0; i < listLines.length; i++) {
            int[] line = listLines[i];
            Map<String, Double> pt1 = landmarks.get(line[0]);
            Map<String, Double> pt2 = landmarks.get(line[1]);
            input.put("x" + i + "_1", pt1.get("x"));
            input.put("y" + i + "_1", pt1.get("y"));
            input.put("x" + i + "_2", pt2.get("x"));
            input.put("y" + i + "_2", pt2.get("y"));
        }
        return input;
    }

    private String mapEmotionToString(double emotion) {
        return switch ((int) emotion) {
            case 0 -> "NEUTRAL";  // Caso para emoção não detectada
            case 1 -> "HAPPINESS";
            case 2 -> "SADNESS/ANGER";  // Emoções combinadas
            case 3 -> "SURPRISE";
            default -> "UNKNOWN";
        };
    }
    

    private Map<String, Object> predictEmotion(Map<String, Double> input) {
        Map<String, Object> result = new HashMap<>();
        Map<String, ?> results = evaluator.evaluate(input);
        results = EvaluatorUtil.decodeAll(results);

        for (Map.Entry<String, ?> entry : results.entrySet()) {
            String key = entry.getKey();
            Object resultValue = entry.getValue();
            if (resultValue instanceof Computable computable) {
                result.put(key, computable.getResult());
            } else {
                result.put(key, resultValue);
            }
        }
        return result;
    }
}