import { FaceMesh } from 'https://cdn.jsdelivr.net/npm/@mediapipe/face_mesh@0.4.1633559619/face_mesh.min.js';
import { Camera } from 'https://cdn.jsdelivr.net/npm/@mediapipe/camera_utils@0.3.1675466862/camera_utils.min.js';
import { drawConnectors, FACEMESH_TESSELATION } from 'https://cdn.jsdelivr.net/npm/@mediapipe/drawing_utils@0.3.1675466124/drawing_utils.min.js';

const video = document.getElementById('webcam');
const canvas = document.getElementById('output_canvas');
const canvasCtx = canvas.getContext('2d');

let runningModel = false;

// Endpoint para realizar a predição de emoção
const predictEmotionEndpoint = '/api/detectEmotionFromLandmarks';

// Configuração do MediaPipe FaceMesh
const faceMesh = new FaceMesh({
    locateFile: (file) => `https://cdn.jsdelivr.net/npm/@mediapipe/face_mesh@0.10.2/dist/${file}`
});
faceMesh.setOptions({
    maxNumFaces: 1,
    refineLandmarks: true,
    minDetectionConfidence: 0.5,
    minTrackingConfidence: 0.5
});

// Lista de linhas para desenhar
const listLines = [
    [130, 27], [130, 23], [27, 133], [23, 133], // olho esquerdo
    [359, 257], [359, 253], [257, 362], [253, 362], // olho direito
    [130, 70], [107, 168], [168, 336], [300, 359], // sobrancelhas e nariz
    [168, 4], // nariz
    [133, 4], [362, 4], // olhos - ponta nariz
    [4, 50], [4, 280], // ponta nariz - bochecha
    [4, 0], // ponta nariz - topo boca
    [70, 105], [105, 107], [107, 108], [108, 104], [104, 71], [71, 70], // sobrancelha esquerda
    [336, 334], [334, 300], [300, 301], [301, 333], [333, 337], [337, 336], // sobrancelha direita
    [61, 40], [61, 91], [40, 0], [91, 17], [0, 270], [17, 321], [270, 291], [321, 291], // contorno da boca
    [50, 61], [280, 291], // bochechas
    [212, 216], [216, 214], [214, 207], [207, 192], [192, 197], // lado boca esquerda zig zag
    [432, 436], [436, 434], [434, 427], [427, 416], [416, 411], // lado boca direita zig zag
    [61, 212], [291, 432]
];

// Função para processar os resultados do MediaPipe
function onResults(results) {
    canvasCtx.save();
    canvasCtx.clearRect(0, 0, canvas.width, canvas.height);
    canvasCtx.drawImage(results.image, 0, 0, canvas.width, canvas.height);

    if (results.multiFaceLandmarks) {
        for (const landmarks of results.multiFaceLandmarks) {
            drawConnectors(canvasCtx, landmarks, FACEMESH_TESSELATION, { color: '#C0C0C070', lineWidth: 1 });
            drawCustomLandmarks(canvasCtx, landmarks);

            // Enviar landmarks para o back-end
            sendLandmarks(landmarks);
        }
    }
    canvasCtx.restore();
}

// Função para desenhar linhas e pontos personalizados
function drawCustomLandmarks(ctx, landmarks) {
    const points = landmarks.map(landmark => ({
        x: landmark.x * canvas.width,
        y: landmark.y * canvas.height
    }));

    // Desenhar as linhas com base na lista de linhas
    ctx.strokeStyle = '#FF0000'; // Cor da linha
    ctx.lineWidth = 2; // Espessura da linha
    ctx.beginPath();

    listLines.forEach(line => {
        const [startIndex, endIndex] = line;
        const startPoint = points[startIndex];
        const endPoint = points[endIndex];
        if (startPoint && endPoint) {
            ctx.moveTo(startPoint.x, startPoint.y);
            ctx.lineTo(endPoint.x, endPoint.y);
        }
    });

    ctx.stroke();

    // Desenhar pontos nos landmarks
    ctx.fillStyle = '#00FF00'; // Cor do ponto
    points.forEach(point => {
        ctx.beginPath();
        ctx.arc(point.x, point.y, 3, 0, 2 * Math.PI);
        ctx.fill();
    });
}

// Função para enviar os landmarks para o back-end
async function sendLandmarks(landmarks) {
    const formattedLandmarks = landmarks.map(landmark => ({
        x: landmark.x,
        y: landmark.y
    }));

    try {
        const response = await fetch(predictEmotionEndpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ landmarks: formattedLandmarks }),
        });

        if (response.ok) {
            const prediction = await response.json();
            updateUI(prediction);
        } else {
            console.error('Erro na predição:', response.statusText);
        }
    } catch (error) {
        console.error('Erro ao enviar landmarks:', error);
    }
}

// Atualiza a interface do usuário com a emoção predita
function updateUI(prediction) {
    const emotionElement = document.getElementById('predicted-emotion');
    const confidenceElement = document.getElementById('confidence');

    if (prediction) {
        emotionElement.textContent = prediction.emotion;
        confidenceElement.textContent = prediction.confidence.toFixed(2);
    }
}

// Função para iniciar a detecção de vídeo
async function startDetection() {
    runningModel = true;
    const camera = new Camera(video, {
        onFrame: async () => {
            if (runningModel) {
                await faceMesh.send({ image: video });
            }
        },
        width: 640,
        height: 480
    });
    camera.start();
}

// Função para parar a detecção de vídeo
function stopDetection() {
    runningModel = false;
}

// Event listeners para os botões de controle da câmera
document.getElementById('startButton').addEventListener('click', startDetection);
document.getElementById('stopButton').addEventListener('click', stopDetection);