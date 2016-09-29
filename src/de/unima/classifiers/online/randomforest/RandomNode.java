package de.unima.classifiers.online.randomforest;

import de.unima.classifiers.Config;
import de.unima.classifiers.Utilities;
import de.unima.classifiers.structure.Result;
import de.unima.classifiers.structure.Sample;

/**
 * Online Random Node. This is the main construct, i.e., covers all information and the references to the succeeding
 * subtrees, and the online random tests for this node. Each tree is initialized with such a node.
 *
 * @author Timo Sztyler
 * @version 29.09.2016
 */
class RandomNode {
    private Config       config;
    private int          numClasses;
    private int          depth;
    private int          label;
    private boolean      isLeaf;
    private double       counter;
    private double       parentCounter;
    private double[]     labelStats;
    private double[]     minFeatRange;
    private double[]     maxFeatRange;
    private RandomNode   leftChildNode;
    private RandomNode   rightChildNode;
    private RandomTest[] randomTests;
    private RandomTest   bestTest;

    RandomNode(Config config, int numClasses, int numFeatures, double[] minFeatRange, double[] maxFeatRange, int depth) {
        this.numClasses = numClasses;
        this.depth = depth;
        this.isLeaf = true;
        this.config = config;
        this.label = -1;
        this.counter = 0.0d;
        this.parentCounter = 0.0d;
        this.labelStats = new double[numClasses];
        this.minFeatRange = minFeatRange;
        this.maxFeatRange = maxFeatRange;

        // create random tests
        this.randomTests = new RandomTest[config.numRandomTests];
        for (int nTest = 0; nTest < config.numRandomTests; nTest++) {
            RandomTest randomTest = new RandomTest(config, numClasses, numFeatures, minFeatRange, maxFeatRange);
            this.randomTests[nTest] = randomTest;
        }
    }

    private RandomNode(Config config, int numClasses, int numFeatures, double[] minFeatRange, double[] maxFeatRange, int depth, double[] parentStats) {
        this.numClasses = numClasses;
        this.depth = depth;
        this.isLeaf = true;
        this.config = config;
        this.label = -1;
        this.counter = 0.0d;
        this.parentCounter = Utilities.getSum(parentStats);
        this.labelStats = parentStats;
        this.minFeatRange = minFeatRange;
        this.maxFeatRange = maxFeatRange;
        this.label = Utilities.getMaxCoeffIndex(this.labelStats);

        // create random tests
        this.randomTests = new RandomTest[config.numRandomTests];
        for (int nTest = 0; nTest < config.numRandomTests; nTest++) {
            this.randomTests[nTest] = new RandomTest(config, numClasses, numFeatures, minFeatRange, maxFeatRange);
        }
    }

    void update(Sample sample) {
        this.counter += sample.getWeight();
        this.labelStats[sample.getLabel()] += sample.getWeight();

        if (this.isLeaf) {
            for (RandomTest randomTest : this.randomTests) {
                randomTest.update(sample);  // update stats
            }

            this.label = Utilities.getMaxCoeffIndex(this.labelStats);
            if (this.shouldISplit()) {  // not pure and more than 200 samples
                this.isLeaf = false;

                int    nTest    = 0;
                int    minIndex = 0;
                double minScore = 1;

                // select best random test
                for (RandomTest randomTest : this.randomTests) {
                    nTest += 1;
                    double score = randomTest.score();

                    if (score < minScore) {
                        minScore = score;
                        minIndex = nTest;
                    }
                }

                // delete everything expect best test
                this.bestTest = this.randomTests[minIndex - 1];
                this.randomTests = null;

                double[][] parentStats = this.bestTest.getStats();  // 0 = trueStats // 1 = falseStats

                // create child nodes
                this.rightChildNode = new RandomNode(this.config, this.numClasses, this.minFeatRange.length, this.minFeatRange, this.maxFeatRange, (this.depth + 1), parentStats[0]);
                this.leftChildNode = new RandomNode(this.config, this.numClasses, this.minFeatRange.length, this.minFeatRange, this.maxFeatRange, (this.depth + 1), parentStats[1]);
            }
        } else {
            if (this.bestTest.eval(sample)) {
                this.rightChildNode.update(sample);
            } else {
                this.leftChildNode.update(sample);
            }
        }
    }

    void eval(Sample sample, Result result) {
        if (this.isLeaf) {  // classify sample
            if ((this.counter + this.parentCounter) != 0) {
                result.setConfidence(this.labelStats.clone());
                result.divideConfidenceByDouble(this.counter + this.parentCounter);
                result.setPrediction(this.label);
            } else {    // there is no information
                double[] conf = new double[this.labelStats.length];
                for (int i = 0; i < conf.length; i++) {
                    conf[i] = 1.0d / ((double) this.numClasses);
                }
                result.setConfidence(conf);
                result.setPrediction(0);
            }
        } else {    // search corresponding leaf
            if (this.bestTest.eval(sample)) {
                this.rightChildNode.eval(sample, result);
            } else {
                this.leftChildNode.eval(sample, result);
            }
        }
    }

    private boolean shouldISplit() {
        boolean isPure = false;

        // check if leaf is pure
        for (int nClass = 0; nClass < this.numClasses; nClass++) {
            if (this.labelStats[nClass] == (this.counter + this.parentCounter)) {
                isPure = true;
                break;
            }
        }

        // returns true if leaf is not pure, has seen more than 'threshold' values, and is not to deep
        return !(isPure || this.depth >= this.config.maxDepth || this.counter < this.config.sampleThreshold);
    }
}