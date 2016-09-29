package de.unima.classifiers.structure;

/**
 * Container that covers a single sample/record, i.e., feature values, the corresponding label, and a optional weight.
 *
 * @author Timo Sztyler
 * @version 29.09.2016
 */
public class Sample {

    private double[] values;
    private int      label;
    private double   weight;
    private int      id;

    public Sample(double[] values, int label, double weight, int id) {
        this.values = values;
        this.label = label;
        this.weight = weight;
        this.id = id;
    }

    public double[] getValues() {
        return values;
    }

    public int getLabel() {
        return label;
    }

    public double getWeight() {
        return weight;
    }

    public int getId() {
        return id;
    }
}