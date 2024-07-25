document.getElementById('imageUpload').addEventListener('change', function(event) {
    let file = event.target.files[0];
    if (!file) return;

    let formData = new FormData();
    formData.append('image', file);

    fetch('/api/detectEmotionImage', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        // Clear previous results
        let emotionResultElement = document.getElementById('emotionResult');
        emotionResultElement.innerHTML = '';

        // Display detected emotions and confidence
        let emotionElement = document.createElement('div');
        emotionElement.innerHTML = `<strong>${data.emotion}</strong>: ${data.confidence.toFixed(2)}%`;
        emotionResultElement.appendChild(emotionElement);

        // Display image with landmarks
        let imgWithLandmarks = new Image();
        imgWithLandmarks.onload = function() {
            let canvas = document.getElementById('imageCanvas');
            let ctx = canvas.getContext('2d');
            canvas.width = imgWithLandmarks.width;
            canvas.height = imgWithLandmarks.height;
            ctx.drawImage(imgWithLandmarks, 0, 0, canvas.width, canvas.height);
        };
        imgWithLandmarks.src = data.imageWithLandmarks;

        console.log("Landmarks: ", data.landmarks); // Log dos landmarks para depuração
        console.log("Resultado da emoção: ", data); // Log para depuração
    })
    .catch(error => {
        console.error('Error:', error);
    });
});
