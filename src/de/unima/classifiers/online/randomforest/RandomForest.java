package de.unima.classifiers.online.randomforest;

import de.unima.classifiers.Config;
import de.unima.classifiers.Utilities;
import de.unima.classifiers.online.Classifier;
import de.unima.classifiers.structure.Result;
import de.unima.classifiers.structure.Sample;

/**
 * Online Random Forest Classifier. Basis structure to train and use this classifier. The number of trees, target
 * classes, and considered features has to be predefined. There is no batch training or testing mode, i.e., each sample
 * has to be passed successively to the classifier.
 *
 * @author Timo Sztyler
 * @version 29.09.2016
 */
public class RandomForest implements Classifier {
    private Config       config;
    private RandomTree[] trees;
    private int[]        counter;
    private int[][]      treeStats;

    public RandomForest(Config config, int numClasses, int numFeatures, double[] minFeatRange, double[] maxFeatRange) {
        this.config = config;

        // init structure
        int numTrees = this.config.numTree;
        this.trees = new RandomTree[numTrees];
        this.counter = new int[numClasses];
        this.treeStats = new int[numTrees][numClasses];

        // create trees
        for (int nTree = 0; nTree < numTrees; nTree++) {
            RandomTree tree = new RandomTree(config, numClasses, numFeatures, minFeatRange, maxFeatRange);
            this.trees[nTree] = tree;
        }
    }

    @Override
    public void update(Sample sample) {
        for (int nTree = 0; nTree < this.config.numTree; nTree++) {
            Result treeResult = new Result();
            int    numTries   = Utilities.poisson(this.config.poissonLambda);

            // bagging based on poisson distribution, sample is ignored if numTries is 0
            if (numTries != 0) {
                for (int nTry = 0; nTry < numTries; nTry++) {
                    this.trees[nTree].update(sample);
                }
            } else {    // generate stats
                this.trees[nTree].eval(sample, treeResult);

                int predicted = Utilities.getMaxCoeffIndex(treeResult.getConfidence());
                this.counter[predicted]++;

                if (predicted != sample.getLabel()) {
                    this.treeStats[nTree][predicted]++;
                }
            }
        }
    }

    @Override
    public void eval(Sample sample, Result result) {
        for (int nTree = 0; nTree < this.config.numTree; nTree++) {
            Result treeResult = new Result();
            this.trees[nTree].eval(sample, treeResult);

            // weight individual results
            if(this.config.weightIndividualResult) {
                double[] weights = Utilities.calcWeights(this.counter, this.treeStats[nTree]);
                result.addConfidence(treeResult.getConfidence(), weights);
            } else {
                // sum up individual results
                result.addConfidence(treeResult.getConfidence());
            }
        }

        // determine result
        result.divideConfidenceByInteger(this.config.numTree);
        int pre = Utilities.getMaxCoeffIndex(result.getConfidence());
        result.setPrediction(pre);
    }
}