package de.unima.classifiers.evaluation;


import de.unima.classifiers.Config;
import de.unima.classifiers.dataset.DataSet;
import de.unima.classifiers.online.Classifier;
import de.unima.classifiers.online.randomforest.RandomForest;
import de.unima.classifiers.structure.Result;
import de.unima.classifiers.structure.Sample;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

/**
 * Cross-Validation. Provides the possibility to perform cross validation, i.e, split the loaded data set into n-folds
 * where stratified sampling is performed. Subsequently, one fold is used for testing and the others for testing.
 *
 * @author Timo Sztyler
 * @version 29.09.2016
 */
public class CrossValidation {
    private DataSet                    dataSet;
    private int                        numOfFolds;
    private int                        numOfRuns;
    private Classifier.Type            ct;
    private Map<Integer, List<Sample>> folds;
    private PrintWriter                out;

    public CrossValidation(Classifier.Type ct, DataSet ds, int folds, int runs) {
        this.dataSet = ds;
        this.numOfFolds = folds;
        this.numOfRuns = runs;
        this.ct = ct;
    }

    public void run() {
        Map<Result, Integer> resultsOfAllRuns = new HashMap<>();

        for (int i = 0; i < this.numOfRuns; i++) {
            if (i != 0) {
                System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" + System.lineSeparator());
                if (this.out != null) {
                    out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" + System.lineSeparator());
                }
            }

            System.out.println("Run: " + i);
            if (this.out != null) {
                out.println("Run: " + i);
            }

            build();
            resultsOfAllRuns.putAll(trainAndTest());
        }

        System.out.println(System.lineSeparator() + "Overall Result of all Runs");
        if (this.out != null) {
            out.println(System.lineSeparator() + "Overall Result of all Runs");
        }

        Evaluation eval = new Evaluation(resultsOfAllRuns, dataSet);
        eval.setOutputWriter(this.out);
        eval.printSummary();
        eval.printDetailedSummary();
        eval.printConfusionMatrix();
    }

    public void setOutputWriter(PrintWriter out) {
        this.out = out;
    }

    private void build() {
        Map<Integer, List<Sample>> samplesByClass = new HashMap<>();
        this.folds = new HashMap<>();

        for (Sample sample : this.dataSet.getSamples()) {
            if (!samplesByClass.containsKey(sample.getLabel())) {
                samplesByClass.put(sample.getLabel(), new ArrayList<Sample>());
            }
            samplesByClass.get(sample.getLabel()).add(sample);
        }

        for (Integer key : samplesByClass.keySet()) {
            Collections.shuffle(samplesByClass.get(key));
            Collections.shuffle(samplesByClass.get(key));
            Collections.shuffle(samplesByClass.get(key));
        }

        for (int i = 0; i < this.numOfFolds; i++) {
            this.folds.put(i, new ArrayList<Sample>());

            for (Integer key : samplesByClass.keySet()) {
                List<Sample> values = samplesByClass.get(key);

                int num    = values.size() / this.numOfFolds;
                int modulo = values.size() % this.numOfFolds;

                int inc = modulo;
                if (i <= modulo) {
                    inc = modulo - (modulo - i);
                }

                for (int j = i * num + inc; j < i * num + inc + num; j++) {
                    this.folds.get(i).add(values.get(j));
                }

                if (i < modulo) {
                    this.folds.get(i).add(values.get(i * num + inc + num));
                }
            }
        }

        for (Integer key : folds.keySet()) {
            Collections.shuffle(folds.get(key));
            Collections.shuffle(folds.get(key));
            Collections.shuffle(folds.get(key));
        }

        // Export folds to files
        try {
            File root = new File("folds");
            root.mkdir();
            File tmp = new File(root, Long.toString(System.currentTimeMillis()));
            tmp.mkdir();
            for (int i = 0; i < 10; i++) {
                int numSamplesTest  = 0;
                int numSamplesTrain = 0;

                for (int j = 0; j < 10; j++) {
                    if (j == i) {
                        numSamplesTest = folds.get(j).size();
                    } else {
                        numSamplesTrain += folds.get(j).size();
                    }
                }

                int numFeatures = folds.get(0).get(0).getValues().length;

                File fold = new File(tmp, "fold" + i);
                fold.mkdir();
                File        train    = new File(fold, "train.data");
                PrintWriter outtrain = new PrintWriter(train);
                outtrain.println(numSamplesTrain + " " + numFeatures);

                File        trainlabel    = new File(fold, "train.label");
                PrintWriter outtrainlabel = new PrintWriter(trainlabel);
                outtrainlabel.println(numSamplesTrain + " 1");

                File        test    = new File(fold, "test.data");
                PrintWriter outtest = new PrintWriter(test);
                outtest.println(numSamplesTest + " " + numFeatures);

                File        testlabel    = new File(fold, "test.label");
                PrintWriter outtestlabel = new PrintWriter(testlabel);
                outtestlabel.println(numSamplesTest + " 1");

                for (int j = 0; j < 10; j++) {
                    if (j == i) {
                        List<Sample> samples = folds.get(j);
                        for (Sample sample : samples) {
                            for (double d : sample.getValues()) {
                                outtest.print(d);
                                outtest.print(" ");
                            }
                            outtestlabel.println(sample.getLabel());
                            outtest.println();
                        }
                    } else {
                        List<Sample> samples = folds.get(j);
                        for (Sample sample : samples) {
                            for (double d : sample.getValues()) {
                                outtrain.print(d);
                                outtrain.print(" ");
                            }
                            outtrainlabel.println(sample.getLabel());
                            outtrain.println();
                        }
                    }
                }

                outtrain.flush();
                outtrain.close();
                outtrainlabel.flush();
                outtrainlabel.close();
                outtest.flush();
                outtest.close();
                outtestlabel.flush();
                outtestlabel.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<Result, Integer> trainAndTest() {
        Map<Result, Integer> overallResults = new HashMap<>();

        for (int i = 0; i < this.numOfFolds; i++) {
            long start = System.currentTimeMillis();

            if (i != 0) {
                System.out.println();
                if (this.out != null) {
                    out.println();
                }
            }
            System.out.println("Fold: " + i);
            if (this.out != null) {
                out.println("Fold: " + i);
            }

            Config     rfc = new Config();
            Classifier classifier;

            switch (this.ct) {
                case ONLINERANDOMFOREST:
                    classifier = new RandomForest(rfc, dataSet.getNumOfClasses(), dataSet.getNumOfFeatures(), dataSet.getMinFeatRange(), dataSet.getMaxFeatRange());
                    break;
                default:
                    return null;
            }

            for (int nEpoch = 0; nEpoch < rfc.numEpochs; nEpoch++) {
                for (int j = 0; j < this.numOfFolds; j++) {
                    if (i == j) {
                        continue;
                    }

                    List<Sample> samples = folds.get(j);
                    Collections.shuffle(samples);

                    for (Sample sample : samples) {
                        classifier.update(sample);
                    }
                }
            }

            System.out.println("Runtime: " + ((System.currentTimeMillis() - start) / 1000.0d));

            Map<Result, Integer> results = new HashMap<>();
            for (Sample sample : folds.get(i)) {
                Result result = new Result(dataSet.getNumOfClasses());
                classifier.eval(sample, result);

                results.put(result, sample.getLabel());
                overallResults.put(result, sample.getLabel());
            }

            Evaluation eval = new Evaluation(results, dataSet);
            eval.setOutputWriter(this.out);
            eval.printSummary();
            eval.printDetailedSummary();
            eval.printConfusionMatrix();
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            if (this.out != null) {
                out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            }
        }

        System.out.println(System.lineSeparator() + "Overall Result");
        if (this.out != null) {
            out.println(System.lineSeparator() + "Overall Result");
        }

        Evaluation eval = new Evaluation(overallResults, dataSet);
        eval.setOutputWriter(this.out);
        eval.printSummary();
        eval.printDetailedSummary();
        eval.printConfusionMatrix();

        return overallResults;
    }
}