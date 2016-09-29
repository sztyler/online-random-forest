package de.unima.classifiers;

/**
 * Default parameter. They have to be modified befor the initialization of a classifier. Changing the parameter
 * afterwards might result in side effects.
 *
 * @author Timo Sztyler
 * @version 29.09.2016
 */
public class Config {
    public int     numTree                = 10;         // 100
    public int     maxDepth               = 20;
    public double  sampleThreshold        = 200;
    public int     numRandomTests         = 20;
    public int     numEpochs              = 10;
    public boolean refineThreshold        = true;       // true=needs more memory
    public int     leafCacheSize          = 100;        // only considered if refineThreshold is true
    public int     genRanThres            = 200;
    public String  scoreMeasure           = "GINI";     // INFO; GINI
    public double  poissonLambda          = 1.0d;       // do not change this value
    public boolean weightIndividualResult = false;
}