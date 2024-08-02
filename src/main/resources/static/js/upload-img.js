document.getElementById('imageUpload').addEventListener('change', function(event) {
    let file = event.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = function(event) {
        const img = new Image();
        img.src = event.target.result;

        img.onload = async function() {
            const canvas = document.getElementById('imageCanvas');
            const ctx = canvas.getContext('2d');
            canvas.width = img.width;
            canvas.height = img.height;
            ctx.drawImage(img, 0, 0, canvas.width, canvas.height);

            // Configuração do MediaPipe FaceMesh
            const faceMesh = new FaceMesh({
                locateFile: (file) => `https://cdn.jsdelivr.net/npm/@mediapipe/face_mesh/${file}`
            });
            faceMesh.setOptions({
                maxNumFaces: 1,
                refineLandmarks: true,
                minDetectionConfidence: 0.5,
                minTrackingConfidence: 0.5
            });

            // Função para processar os resultados do MediaPipe
            function onResults(results) {
                ctx.save();
                ctx.clearRect(0, 0, canvas.width, canvas.height);
                ctx.drawImage(img, 0, 0, canvas.width, canvas.height);

                if (results.multiFaceLandmarks) {
                    for (const landmarks of results.multiFaceLandmarks) {
                        drawConnectors(ctx, landmarks, FACEMESH_TESSELATION, { color: '#C0C0C070', lineWidth: 1 });
                        drawConnectors(ctx, landmarks, FACEMESH_RIGHT_EYE, { color: '#FF3030' });
                        drawConnectors(ctx, landmarks, FACEMESH_LEFT_EYE, { color: '#30FF30' });
                        drawConnectors(ctx, landmarks, FACEMESH_FACE_OVAL, { color: '#E0E0E0' });

                        // Enviar landmarks para o back-end
                        sendLandmarks(landmarks);
                    }
                }
                ctx.restore();
            }

            // Função para enviar os landmarks para o back-end
            async function sendLandmarks(landmarks) {
                const formattedLandmarks = landmarks.map(landmark => ({
                    x: landmark.x,
                    y: landmark.y
                }));

                try {
                    const response = await fetch('/api/detectEmotionFromLandmarks', {
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
                const emotionElement = document.getElementById('emotionResult');
                emotionElement.innerHTML = `<strong>${prediction.emotion}</strong>: ${prediction.confidence.toFixed(2)}%`;
            }

            // Inicializa o modelo FaceMesh e detecta landmarks
            faceMesh.send({ image: img }).then(() => {
                onResults({ multiFaceLandmarks: [faceMesh.getLandmarks()] });
            });
        };

        reader.readAsDataURL(file);
    };
});