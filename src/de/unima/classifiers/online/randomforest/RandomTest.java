package de.unima.classifiers.online.randomforest;

import de.unima.classifiers.Config;
import de.unima.classifiers.Utilities;
import de.unima.classifiers.structure.Sample;

/**
 * Random Test. This kind of tests are generated for each new node. The number of tests are predefined and each test
 * covers a specific attribute/feature and a corresponding threshold. The threshold is generated at random. This tests
 * are used to determine the best split for a node. If a decision is made, the tests are removed.
 *
 * @author Timo Sztyler
 * @version 29.09.2016
 */
class RandomTest {
    private Config     config;
    private int        numClasses;
    private int        featureId;
    private int        cacheCounter;
    private double     threshold;
    private double     trueCount;
    private double     falseCount;
    private double     minFeatRange;
    private double     maxFeatRange;
    private double[]   trueStats;
    private double[]   falseStats;
    private double[][] cachedValues;

    RandomTest(Config config, int numClasses, int numFeatures, double[] minFeatRange, double[] maxFeatRange) {
        this.numClasses = numClasses;
        this.trueCount = 0.0d;
        this.falseCount = 0.0d;
        this.trueStats = new double[numClasses];
        this.falseStats = new double[numClasses];
        this.featureId = (int) Utilities.randDouble(0, numFeatures);
        this.config = config;

        if (config.refineThreshold) {   // cache up to 'leafCacheSize' samples and optimize threshold
            this.cachedValues = new double[config.leafCacheSize][2];
            this.cacheCounter = 0;
            this.minFeatRange = minFeatRange[this.featureId];
            this.maxFeatRange = maxFeatRange[this.featureId];
        } else {
            this.threshold = Utilities.randDouble(minFeatRange[this.featureId], maxFeatRange[this.featureId]);    // Extremely Randomized Forest
        }
    }

    void update(Sample sample) {
        if (this.config.refineThreshold) {
            this.cachedValues[this.cacheCounter % this.config.leafCacheSize][0] = sample.getValues()[this.featureId];
            this.cachedValues[this.cacheCounter % this.config.leafCacheSize][1] = sample.getLabel();
            this.cacheCounter++;
        } else {
            this.updateStats(sample, this.eval(sample));
        }
    }

    boolean eval(Sample sample) {
        return sample.getValues()[this.featureId] > this.threshold;
    }

    double score() {
        switch (this.config.scoreMeasure) {
            case "GINI":
                return scoreGINI();
            case "INFO":
                return scoreINFO();
            default:
                return scoreINFO(); // default measure
        }
    }

    double[][] getStats() {
        double[][] stats = new double[2][];
        stats[0] = new double[this.trueStats.length];
        stats[1] = new double[this.falseStats.length];

        System.arraycopy(this.trueStats, 0, stats[0], 0, this.trueStats.length);
        System.arraycopy(this.falseStats, 0, stats[1], 0, this.falseStats.length);

        return stats;
    }

    // GINI Index score
    private double scoreGINI() {
        if (this.config.refineThreshold) {
            optimizeThreshold();
            this.cachedValues = null;   // delete cache
        }

        double trueScore  = 0.0d;
        double falseScore = 0.0d;

        if (this.trueCount != 0) {
            for (int nClass = 0; nClass < this.numClasses; nClass++) {
                double p = this.trueStats[nClass] / this.trueCount;
                trueScore += p * (1.0d - p);
            }
        }

        if (this.falseCount != 0) {
            for (int nClass = 0; nClass < this.numClasses; nClass++) {
                double p = this.falseStats[nClass] / this.falseCount;
                falseScore += p * (1.0d - p);
            }
        }

        return (this.trueCount * trueScore + this.falseCount * falseScore) / (this.trueCount + this.falseCount + 1e-16);
    }


    // Information Gain score
    private double scoreINFO() {
        if (config.refineThreshold) {
            optimizeThreshold();
            this.cachedValues = null;
        }

        double info  = 0.0;
        double total = this.trueCount + this.falseCount;

        // determine entropy for each class (position)
        for (int nClass = 0; nClass < this.numClasses; nClass++) {
            double entropy = 0.0;

            if (this.trueStats[nClass] != 0) {
                double p = this.trueStats[nClass] / (this.trueStats[nClass] + this.falseStats[nClass]);
                entropy += p * (Math.log(p)/Math.log(2.0d));

            }
            if (this.falseStats[nClass] != 0) {
                double p = this.falseStats[nClass] / (this.trueStats[nClass] + this.falseStats[nClass]);
                entropy += p * (Math.log(p)/Math.log(2.0d));
            }

            entropy *= -1;
            info += ((this.falseStats[nClass] + this.trueStats[nClass]) / (total)) * entropy;
        }

        // determine overall entropy
        double overEntropy = 0.0;
        if (this.trueCount != 0) {
            overEntropy += (this.trueCount / (total)) * (Math.log(this.trueCount / (total)/Math.log(2.0d)));
        }
        if (this.falseCount != 0) {
            overEntropy += (this.falseCount / (total)) * (Math.log(this.falseCount / (total)/Math.log(2.0d)));
        }
        overEntropy *= -1;

        // return information gain
        return -1.0 * (overEntropy - info);
    }


    private void updateStats(Sample sample, boolean decision) {
        if (decision) {
            this.trueCount += sample.getWeight();
            this.trueStats[sample.getLabel()] += sample.getWeight();
        } else {
            this.falseCount += sample.getWeight();
            this.falseStats[sample.getLabel()] += sample.getWeight();
        }
    }

    ///--------------------------------------------------TODO----------------------------------------------------------

    private void optimizeThreshold() {
        double   bestThres      = this.threshold;
        double   bestTrueCount  = this.trueCount;
        double   bestFalseCount = this.falseCount;
        double[] bestTrueStats  = this.trueStats;
        double[] bestFalseStats = this.falseStats;

        double bestScore = Double.MAX_VALUE; //internalScore(this.trueStats, this.trueCount, this.falseStats, this.falseCount);

        for (int i = 0; i < config.genRanThres; i++) {
            double   trueCount  = 0;
            double   falseCount = 0;
            double[] trueStats  = new double[numClasses];
            double[] falseStats = new double[numClasses];
            this.threshold = Utilities.randDouble(minFeatRange, maxFeatRange);

            for (int j = 0; j < config.leafCacheSize; j++) {
                if (cachedValues[j][0] > this.threshold) {
                    trueCount++;
                    trueStats[(int) cachedValues[j][1]]++;
                } else {
                    falseCount++;
                    falseStats[(int) cachedValues[j][1]]++;
                }
            }

            double tmpScore = internalScore(trueStats, trueCount, falseStats, falseCount);

            if (tmpScore < bestScore) {
                bestThres = this.threshold;
                bestScore = tmpScore;
                bestTrueCount = trueCount;
                bestFalseCount = falseCount;
                bestTrueStats = trueStats;
                bestFalseStats = falseStats;
            }
        }

        this.threshold = bestThres;
        this.trueCount = bestTrueCount;
        this.falseCount = bestFalseCount;
        this.trueStats = bestTrueStats;
        this.falseStats = bestFalseStats;
    }

    private double internalScore(double[] trueStats, double trueCount, double[] falseStats, double falseCount) {
        switch (this.config.scoreMeasure) {
            case "GINI":
                return internalGini(trueStats, trueCount, falseStats, falseCount);
            case "INFO":
                return internalInfo(trueStats, trueCount, falseStats, falseCount);
            default:
                return 0.0d;
        }
    }

    private double internalGini(double[] trueStats, double trueCount, double[] falseStats, double falseCount) {
        double trueScore  = 0.0d;
        double falseScore = 0.0d;

        for (int nClass = 0; nClass < this.numClasses; nClass++) {
            if (trueCount != 0) {
                double p = trueStats[nClass] / trueCount;
                trueScore += p * (1.0d - p);
            }

            if (falseCount != 0) {
                double p = falseStats[nClass] / falseCount;
                falseScore += p * (1.0d - p);
            }
        }

        return (trueCount * trueScore + falseCount * falseScore) / (trueCount + falseCount + 1e-16);
    }

    private double internalInfo(double[] trueStats, double trueCount, double[] falseStats, double falseCount) {
        double info  = 0.0;
        double total = trueCount + falseCount;

        for (int nClass = 0; nClass < this.numClasses; nClass++) {

            double entropy = 0.0;

            if (trueStats[nClass] != 0) {
                double p = trueStats[nClass] / (trueStats[nClass] + falseStats[nClass]);
                entropy += p * Math.log(p);

            }
            if (falseStats[nClass] != 0) {
                double p = falseStats[nClass] / (trueStats[nClass] + falseStats[nClass]);
                entropy += p * Math.log(p);
            }

            entropy *= -1;
            info += ((falseStats[nClass] + trueStats[nClass]) / (total)) * entropy;
        }

        double overEntropy = 0.0;
        if (trueCount != 0) {
            overEntropy += (trueCount / (total)) * Math.log(trueCount / (total));
        }
        if (falseCount != 0) {
            overEntropy += (falseCount / (total)) * Math.log(falseCount / (total));
        }

        overEntropy *= -1;

        return -1.0 * (overEntropy - info);
    }
}