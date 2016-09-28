package de.unima.classifiers;

public class Config {
    public int     numTree                = 10; // 100
    public int     maxDepth               = 20;
    public double  sampleThreshold        = 200;
    public int     numRandomTests         = 20;
    public int     numEpochs              = 10;
    public boolean refineThreshold        = true;
    public int     leafCacheSize          = 100;
    public int     genRanThres            = 200;
    public String  scoreMeasure           = "GINI"; // INFO; GINI
    public double  poissonLambda          = 1.0d;
    public boolean weightIndividualResult = false;
}