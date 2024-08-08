package com.example.emotion_recognition_api.camera;

import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.LoadingModelEvaluatorBuilder;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CameraTest {

    private Evaluator evaluator;

    public CameraTest() {
        try {
            File pmmlFile = new File("src/main/resources/model/emotion_recognition_model.pmml"); // Substitua pelo caminho do seu arquivo PMML
            this.evaluator = new LoadingModelEvaluatorBuilder()
                    .load(pmmlFile)
                    .build();

            // Verifica se o modelo foi carregado corretamente
            this.evaluator.verify();
            System.out.println("Modelo carregado com sucesso.");

        } catch (Exception e) {
            System.err.println("Erro ao carregar o modelo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            // Obtenha os campos de entrada do modelo
            List<InputField> inputFields = evaluator.getInputFields();
            System.out.println("Detalhes dos Campos de Entrada:");
            for (InputField field : inputFields) {
                System.out.println("Nome: " + field.getName());
                // Aqui você pode adicionar outros detalhes se necessário
            }

            // Dados fictícios para teste, substitua pelos dados reais conforme necessário
            Map<String, Double> testData = new HashMap<>();
            // Adicione entradas fictícias conforme os nomes dos campos do modelo
            testData.put("x0", 0.5); // Exemplo de entrada
            testData.put("y0", 0.7); // Exemplo de entrada

            // Avalie o modelo com os dados de teste
            Map<String, ?> results = evaluator.evaluate(testData);
            System.out.println("Resultados da Avaliação: " + results);

        } catch (Exception e) {
            System.err.println("Erro ao avaliar o modelo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}