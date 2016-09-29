package de.unima.classifiers.structure;

/**
 * Container that is used to store and provide classification results but also confidence values regarding all possible
 * target classes.
 *
 * @author Timo Sztyler
 * @version 29.09.2016
 */
public class Result {

    private double[] confidence;
    private int      prediction;

    public Result() {

    }

    public Result(int numClasses) {
        this.confidence = new double[numClasses];
    }

    public void setConfidence(double[] confidence) {
        this.confidence = confidence;
    }

    public void addConfidence(double[] confidence) {
        if (this.confidence == null) {
            setConfidence(confidence.clone());
            return;
        }

        for (int i = 0; i < this.confidence.length; i++) {
            this.confidence[i] += confidence[i];
        }
    }

    public void addConfidence(double[] confidence, double[] weights) {
        if (this.confidence == null) {
            this.confidence = new double[confidence.length];
        }

        for (int i = 0; i < this.confidence.length; i++) {
            this.confidence[i] += confidence[i] * weights[i];
        }
    }

    public void divideConfidenceByInteger(int value) {
        for (int i = 0; i < this.confidence.length; i++) {
            this.confidence[i] /= (double) value;
        }
    }

    public void divideConfidenceByDouble(double value) {
        for (int i = 0; i < this.confidence.length; i++) {
            this.confidence[i] /= value;
        }
    }

    public void setPrediction(int prediction) {
        this.prediction = prediction;
    }

    public double[] getConfidence() {
        return confidence;
    }

    public int getPrediction() {
        return prediction;
    }
}