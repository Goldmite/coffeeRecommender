package org.recsys.demo;

import org.recsys.dto.recommendation.PreparedTrainingData;
import org.recsys.dto.recommendation.TrainedModel;
import org.recsys.dto.recommendation.TrainingResult;
import org.recsys.service.MatrixFactorizationModel;
import org.recsys.service.TrainingDataService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@Profile("test")
@RequiredArgsConstructor
public class modelTestRunner implements CommandLineRunner {

    private final TrainingDataService trainingDataService;
    private final MatrixFactorizationModel modelService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting Matrix Factorization Test Run...");

        // 1. Prepare the data
        PreparedTrainingData data = trainingDataService.prepareData(true);

        if (data.triplets().isEmpty()) {
            System.out.println("❌ No interactions found in database. Add some ratings first!");
            return;
        }

        System.out.println("📊 Data Loaded: " + data.triplets().size() + " interactions.");
        System.out.println("🌍 Global Mean Rating: " + data.globalMean());

        // 2. Run the training
        TrainingResult result = modelService.train(data);
        TrainedModel model = result.model();
        System.out.println("Final RMSE: " + result.rmse());

        System.out.println("✅ Training Complete!");

        // 3. Test a dummy prediction
        float predicted = model.predict(1L, 1L);
        System.out.println("🔮 Prediction for User 1 on Coffee 1: " + predicted);
    }
}
