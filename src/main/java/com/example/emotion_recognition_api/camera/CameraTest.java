package com.example.emotion_recognition_api.camera;

import com.example.emotion_recognition_api.service.EmotionRecognitionService;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;
import org.bytedeco.opencv.global.opencv_highgui;
import org.bytedeco.opencv.global.opencv_imgproc;
import java.util.List;

public class CameraTest {
    static {
        // Substitua "opencv_java4100" pelo nome da biblioteca que você tem
        //System.loadLibrary("opencv_java4100");
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.load("/usr/local/lib/libopencv_java4100.so");

    }

    private final EmotionRecognitionService emotionRecognitionService;

    public CameraTest(EmotionRecognitionService emotionRecognitionService) {
        this.emotionRecognitionService = emotionRecognitionService;
    }

    public void start() {
        @SuppressWarnings("resource")
        VideoCapture capture = new VideoCapture(0);

        if (!capture.isOpened()) {
            System.out.println("Erro ao abrir a câmera.");
            return;
        }

        Mat frame = new Mat();
        while (true) {
            capture.read(frame);
            if (frame.empty()) {
                System.out.println("Erro ao capturar o vídeo.");
                break;
            }

            // Extraí os landmarks e desenha no frame
            List<Point> landmarks = emotionRecognitionService.extractLandmarks(frame);
            drawLandmarks(frame, landmarks);

            opencv_highgui.imshow("Câmera", frame);
            if (opencv_highgui.waitKey(30) >= 0) {
                break;
            }
        }

        capture.release();
        opencv_highgui.destroyAllWindows();
    }

    private void drawLandmarks(Mat image, List<Point> landmarks) {
        Scalar color = new Scalar(0, 255, 0, 0); // Cor verde
    
        for (Point point : landmarks) {
            opencv_imgproc.circle(image, point, 3, color, -1, 8, 0);
        }
    }
}