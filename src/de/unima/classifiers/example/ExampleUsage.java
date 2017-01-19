package de.unima.classifiers.example;

import de.unima.classifiers.Config;
import de.unima.classifiers.dataset.ARFF;
import de.unima.classifiers.dataset.DataSet;
import de.unima.classifiers.evaluation.Evaluation;
import de.unima.classifiers.online.randomforest.RandomForest;
import de.unima.classifiers.structure.Result;
import de.unima.classifiers.structure.Sample;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Example how to use the online random forest classifier
 *
 * @author Timo Sztyler
 * @version 17.01.2017
 */
public class ExampleUsage {
    public static void main(String[] args) throws Exception {
        // load example data set
        DataSet arffTrain = new ARFF();
        arffTrain.setFeatureFilter(new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 15, 16, 17, 21, 22, 23, 27, 28, 29, 36, 37, 38, 39, 40, 41, 46, 47, 49}, false);
        File        arffTrainFile = new File("data/data_train.arff");
        InputStream isTrain       = new FileInputStream(arffTrainFile);
        arffTrain.load(isTrain);

        DataSet arffTest = new ARFF();
        arffTest.setFeatureFilter(new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 15, 16, 17, 21, 22, 23, 27, 28, 29, 36, 37, 38, 39, 40, 41, 46, 47, 49}, false);
        File        arffTestFile = new File("data/data_test.arff");
        InputStream isTest       = new FileInputStream(arffTestFile);
        arffTest.load(isTest);

        // create config
        Config config = new Config();
        config.numTree = 10;
        config.refineThreshold = false;

        // create classifier
        RandomForest rf = new RandomForest(config, arffTrain.getNumOfClasses(), arffTrain.getNumOfFeatures(), arffTrain.getMinFeatRange(), arffTrain.getMaxFeatRange());

        // train classifier
        for (int nEpoch = 0; nEpoch < config.numEpochs; nEpoch++) {
            arffTrain.randomize();

            for (Sample sample : arffTrain.getSamples()) {
                rf.update(sample);
            }
        }

        // test classifier
        Map<Result, Integer> results = new HashMap<>();

        for (Sample sample : arffTest.getSamples()) {
            Result result = new Result(arffTest.getNumOfClasses());
            rf.eval(sample, result);
            results.put(result, sample.getLabel());
        }

        Evaluation eval = new Evaluation(results, arffTest);
        eval.printSummary();
        eval.printDetailedSummary();
        eval.printConfusionMatrix();

        // perform 10-fold cross validation and repeat it 10-times
        //CrossValidation cs = new CrossValidation(Classifier.Type.ONLINERANDOMFOREST, arffTrain, 10, 10);
        //cs.run();
    }
}