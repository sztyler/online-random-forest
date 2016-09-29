package de.unima.classifiers.online;

import de.unima.classifiers.structure.Result;
import de.unima.classifiers.structure.Sample;

/**
 * Interface for classifier modules.
 *
 * @author Timo Sztyler
 * @version 29.09.2016
 */
public interface Classifier {
    enum Type {
        ONLINERANDOMFOREST;
    }

    void update(Sample sample);

    void eval(Sample sample, Result result);
}