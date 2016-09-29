package de.unima.classifiers.dataset;

import de.unima.classifiers.structure.Sample;

import java.util.List;
import java.util.Map;

/**
 * Interface for data set structures that should be used to train or test a classifier.
 *
 * @author Timo Sztyler
 * @version 29.09.2016
 */
public interface DataSet {
    void load(String path);

    void load(String path, Map<String, Integer> classLabels);

    void randomize();

    void setFeatureFilter(Integer[] featureFilter, boolean invert);

    List<Sample> getSamples();

    int getNumOfClasses();

    int getNumOfFeatures();

    double[] getMinFeatRange();

    double[] getMaxFeatRange();

    String getLabel(int id);

    Map<String, Integer> getClassLabels();
}