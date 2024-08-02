package com.example.emotion_recognition_api.camera;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;
import org.bytedeco.opencv.global.opencv_highgui;

public class CameraTest {
    static {
        // Substitua pelo caminho correto da biblioteca OpenCV em seu sistema
        System.load("/usr/local/lib/libopencv_java4100.so"); // Caminho para a biblioteca nativa OpenCV
    }

    private VideoCapture capture;

    public CameraTest() {
        capture = new VideoCapture(0);
    }

    public void start() {
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

            opencv_highgui.imshow("Câmera", frame);

            if (opencv_highgui.waitKey(30) == 'q') {
                break;
            }
        }

        stop();
    }

    public void stop() {
        if (capture != null) {
            capture.release();
        }
        opencv_highgui.destroyAllWindows();
    }
}