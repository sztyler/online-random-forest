package de.unima.classifiers.online.randomforest;

import de.unima.classifiers.Config;
import de.unima.classifiers.online.Classifier;
import de.unima.classifiers.structure.Result;
import de.unima.classifiers.structure.Sample;

class RandomTree implements Classifier {
    private RandomNode rootNode;

    RandomTree(Config config, int numClasses, int numFeatures, double[] minFeatRange, double[] maxFeatRange) {
        this.rootNode = new RandomNode(config, numClasses, numFeatures, minFeatRange, maxFeatRange, 0);
    }

    @Override
    public void update(Sample sample) {
        this.rootNode.update(sample);
    }

    @Override
    public void eval(Sample sample, Result result) {
        this.rootNode.eval(sample, result);
    }
}