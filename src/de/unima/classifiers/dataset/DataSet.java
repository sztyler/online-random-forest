package de.unima.classifiers.dataset;

import de.unima.classifiers.structure.Sample;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Interface for data set structures that should be used to train or test a classifier.
 *
 * @author Timo Sztyler
 * @version 19.01.2017
 */
public interface DataSet {
    void load(InputStream is);

    void load(InputStream is, Map<String, Integer> classLabels);

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