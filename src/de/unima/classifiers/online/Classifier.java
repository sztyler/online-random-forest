package de.unima.classifiers.online;

import de.unima.classifiers.structure.Result;
import de.unima.classifiers.structure.Sample;

public interface Classifier {
    enum Type {
        ONLINERANDOMFOREST;
    }

    void update(Sample sample);

    void eval(Sample sample, Result result);
}