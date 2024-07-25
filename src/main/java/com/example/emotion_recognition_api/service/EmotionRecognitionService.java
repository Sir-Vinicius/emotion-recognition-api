package com.example.emotion_recognition_api.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import org.dmg.pmml.Model;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.Computable;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.EvaluatorUtil;
import org.jpmml.evaluator.ModelEvaluator;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.model.PMMLUtil;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import jakarta.xml.bind.JAXBException;

@Service
public class EmotionRecognitionService {

    private static final String ERROR_PROCESSING_IMAGE = "Erro ao processar a imagem";
    private static final Scalar LANDMARK_COLOR = new Scalar(0, 255, 0, 0); // Cor verde para os pontos
    private final Evaluator evaluator;
    private final CascadeClassifier faceDetector;

    private final int[][] listLines = {
        {130, 27}, {130, 23}, {27, 133}, {23, 133}, // olho esquerdo 
        {359, 257}, {359, 253}, {257, 362}, {253, 362}, // olho direito 
        {130, 70}, {107, 168}, {168, 336}, {300, 359}, // retas sombracelhas olhos e nariz
        {168, 4}, // nariz 
        {133, 4}, {362, 4}, // olhos - ponta nariz
        {4, 50}, {4, 280}, // ponta nariz - bocheca
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
            this.faceDetector = new CascadeClassifier();
            loadCascadeClassifierModel("haarcascade_frontalface_default.xml");
        } catch (IOException | SAXException | ParserConfigurationException | JAXBException e) {
            String errorMessage = "Falha ao carregar o modelo PMML ou o arquivo de detecção de rosto: " + e.getMessage();
            throw new RuntimeException(errorMessage);
        }
    }

    private Evaluator loadPmmlModel() throws IOException, SAXException, ParserConfigurationException, JAXBException {
        try (InputStream is = new ClassPathResource("emotion_recognition_model.pmml").getInputStream()) {
            PMML pmml = PMMLUtil.unmarshal(is);
            Model model = pmml.getModels().get(0);
            ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
            ModelEvaluator<?> modelEvaluator = modelEvaluatorFactory.newModelEvaluator(pmml, model);
            return modelEvaluator;
        }
    }

    private void loadCascadeClassifierModel(String modelFileName) throws IOException {
        try (InputStream is = new ClassPathResource(modelFileName).getInputStream()) {
            // Converte o InputStream para um caminho de arquivo temporário
            String tempFilePath = new java.io.File("temp.xml").getAbsolutePath();
            java.nio.file.Files.copy(is, java.nio.file.Paths.get(tempFilePath));
            faceDetector.load(tempFilePath);
            // Limpa o arquivo temporário após carregar
            new java.io.File(tempFilePath).delete();
        }
    }

    public Map<String, Object> detectEmotionFromImage(MultipartFile file) {
        return processFile(file);
    }

    public Map<String, Object> detectEmotionFromVideo(MultipartFile file) {
        return processFile(file);
    }

    private Map<String, Object> processFile(MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            byte[] decodedBytes = file.getBytes();
            Mat matOfByte = new Mat(decodedBytes);
            Mat image = opencv_imgcodecs.imdecode(matOfByte, opencv_imgcodecs.IMREAD_UNCHANGED);
            Map<String, Object> result = extractLandmarksAndPredictEmotion(image);
            response.putAll(result);
        } catch (IOException e) {
            response.put("error", ERROR_PROCESSING_IMAGE);
        }
        return response;
    }

    public Map<String, Object> extractLandmarksAndPredictEmotion(Mat image) {
        Map<String, Object> response = new HashMap<>();
        Mat gray = new Mat();
        opencv_imgproc.cvtColor(image, gray, opencv_imgproc.COLOR_BGR2GRAY);
        opencv_imgproc.equalizeHist(gray, gray);

        List<Point> landmarks = extractLandmarks(image);
        for (Point point : landmarks) {
            opencv_imgproc.circle(image, point, 3, LANDMARK_COLOR, -1, 8, 0);
        }

        ByteBuffer buffer = ByteBuffer.allocate((int) (image.total() * image.elemSize()));
        opencv_imgcodecs.imencode(".jpg", image, buffer);
        byte[] encodedImageBytes = new byte[buffer.remaining()];
        buffer.get(encodedImageBytes);
        String imageWithLandmarksBase64 = Base64.getEncoder().encodeToString(encodedImageBytes);

        Map<String, Double> input = prepareInput(landmarks);
        Map<String, ?> prediction = predictEmotion(input);

        double predictedEmotion = (Double) prediction.get("predictedEmotion");
        double confidence = (Double) prediction.get("confidence");

        response.put("emotion", mapEmotionToString(predictedEmotion));
        response.put("confidence", confidence);
        response.put("landmarks", landmarks);
        response.put("imageWithLandmarks", "data:image/jpeg;base64," + imageWithLandmarksBase64);

        return response;
    }

    public List<Point> extractLandmarks(Mat image) {
        List<Point> landmarks = new ArrayList<>();
        RectVector faces = new RectVector();
        faceDetector.detectMultiScale(image, faces);

        if (faces.size() > 0) {
            // Adicione pontos fixos para a imagem, você pode adicionar mais conforme necessário
            landmarks.addAll(getFixedLandmarks());
        }
        return landmarks;
    }

    private List<Point> getFixedLandmarks() {
        List<Point> landmarks = new ArrayList<>();
        // Adicione pontos fixos aqui
        landmarks.add(new Point(130, 27));
        landmarks.add(new Point(130, 23));
        // Adicione todos os pontos necessários...
        return landmarks;
    }

    private Map<String, Double> prepareInput(List<Point> landmarks) {
        Map<String, Double> input = new HashMap<>();
        for (int i = 0; i < listLines.length; i++) {
            int[] line = listLines[i];
            Point pt1 = landmarks.get(line[0]);
            Point pt2 = landmarks.get(line[1]);
            input.put("x" + i + "_1", (double) pt1.x());
            input.put("y" + i + "_1", (double) pt1.y());
            input.put("x" + i + "_2", (double) pt2.x());
            input.put("y" + i + "_2", (double) pt2.y());
        }
        return input;
    }

    private String mapEmotionToString(double emotion) {
        return switch ((int) emotion) {
            case 0 -> "ANGER";
            case 1 -> "HAPPINESS";
            case 2 -> "NEUTRAL";
            case 3 -> "SADNESS";
            case 4 -> "SURPRISE";
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
            if (resultValue instanceof Computable) {
                result.put(key, ((Computable) resultValue).getResult());
            } else {
                result.put(key, resultValue);
            }
        }
        return result;
    }
}