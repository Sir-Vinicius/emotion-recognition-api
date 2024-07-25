const video = document.getElementById('webcam');
const canvas = document.getElementById('output_canvas');
const canvasCtx = canvas.getContext('2d');
const videoWidth = 640;

let runningModel = false;

// Endpoint para realizar a predição de emoção
const predictEmotionEndpoint = '/api/detectEmotionVideo';

// Função para iniciar a detecção de vídeo e predizer emoção
async function startDetection() {
    runningModel = true;

    function detectEmotion() {
        if (!runningModel) return;

        // Captura o quadro do vídeo no canvas
        canvasCtx.drawImage(video, 0, 0, videoWidth, video.videoHeight);

        // Converte o conteúdo do canvas para Data URL
        const dataUrl = canvas.toDataURL('image/jpeg');

        // Realiza a predição usando o modelo PMML
        predictEmotion(dataUrl)
            .then(prediction => {
                updateUI(prediction);
            })
            .catch(error => {
                console.error('Erro na predição:', error);
            });

        // Solicita a próxima detecção de quadro
        requestAnimationFrame(detectEmotion);
    }

    detectEmotion();
}

// Função para parar a detecção de vídeo
function stopDetection() {
    runningModel = false;
}

// Função para predizer a emoção usando o modelo PMML
async function predictEmotion(dataUrl) {
    try {
        const response = await fetch(predictEmotionEndpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ videoData: dataUrl }),
        });

        if (response.ok) {
            const prediction = await response.json();
            return prediction;
        } else {
            console.error('Erro na predição:', response.statusText);
            return null;
        }
    } catch (error) {
        console.error('Erro na predição:', error);
        return null;
    }
}

// Atualiza a interface do usuário com a emoção predita
function updateUI(prediction) {
    if (prediction) {
        const emotionElement = document.getElementById('predicted-emotion');
        const confidenceElement = document.getElementById('confidence');

        emotionElement.textContent = prediction.emotion;
        confidenceElement.textContent = prediction.confidence.toFixed(2);
    }
}

// Event listeners para os botões de controle da câmera
document.getElementById('startButton').addEventListener('click', startDetection);
document.getElementById('stopButton').addEventListener('click', stopDetection);

// Inicia a detecção de emoção ao carregar a página
startDetection(); // Isso presumiria que o modelo já está carregado no backend