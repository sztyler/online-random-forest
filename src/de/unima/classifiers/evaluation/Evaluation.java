package de.unima.classifiers.evaluation;

import de.unima.classifiers.dataset.DataSet;
import de.unima.classifiers.structure.Result;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Map;

/**
 * Evaluation. This module provides the possibility to calculate common statistics concerning of the performance of the
 * trained and testes classification model. This includes, precision, recall, F-measure, confusion matrix, etc.
 *
 * @author Timo Sztyler
 * @version 29.09.2016
 */
public class Evaluation {
    private Map<Result, Integer> results;
    private int                  numOfClasses;
    private DataSet              ds;
    private PrintWriter          out;

    public Evaluation(Map<Result, Integer> results, DataSet ds) {
        this.results = results;
        this.numOfClasses = ds.getNumOfClasses();
        this.ds = ds;
    }

    public void setOutputWriter(PrintWriter out) {
        this.out = out;
    }

    public void printConfusionMatrix() {
        int[][] matrix = new int[this.numOfClasses][this.numOfClasses];

        for (Result key : this.results.keySet()) {
            matrix[this.results.get(key)][key.getPrediction()]++;
        }

        double[] length = new double[this.numOfClasses];

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (length[i] < matrix[j][i]) {
                    length[i] = matrix[j][i];
                }
            }
        }

        for (int i = 0; i < matrix.length; i++) {
            char   c       = (char) (97 + i);
            String largest = String.valueOf(length[i]);
            String s       = String.valueOf(c);

            while (s.length() < largest.length()) {
                s = " " + s;
            }
            System.out.print(s);
            if (this.out != null) {
                out.print(s);
            }
        }
        System.out.println("   <-- classified as");
        if (this.out != null) {
            out.println("   <-- classified as");
        }

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                String value   = String.valueOf(matrix[i][j]);
                String largest = String.valueOf(length[j]);

                while (value.length() < largest.length()) {
                    value = " " + value;
                }

                System.out.print(value);
                if (this.out != null) {
                    out.print(value);
                }
            }
            String label = ds.getLabel(i);
            System.out.println(" |   " + (char) (97 + i) + " = " + label);
            if (this.out != null) {
                out.println(" |   " + (char) (97 + i) + " = " + label);
            }
        }

        System.out.println();
        if (this.out != null) {
            out.println();
        }
    }

    public void printDetailedSummary() {
        double[][] summary = new double[this.numOfClasses + 1][5];

        int[][] matrix = new int[this.numOfClasses][this.numOfClasses];

        for (Result key : this.results.keySet()) {
            matrix[this.results.get(key)][key.getPrediction()]++;
        }

        int   totalNumberOfInstances = 0;
        int[] instancesPerClass      = new int[this.numOfClasses];
        for (int j = 0; j < this.numOfClasses; j++) {
            for (int k = 0; k < this.numOfClasses; k++) {
                totalNumberOfInstances += matrix[j][k];
            }
        }

        for (int i = 0; i < this.numOfClasses; i++) {

            double tp = matrix[i][i];
            double fp = 0.0;
            double fn = 0.0;
            double tn = 0.0;

            for (int j = 0; j < this.numOfClasses; j++) {
                for (int k = 0; k < this.numOfClasses; k++) {
                    if (j != i && k != i) {
                        tn += matrix[j][k];
                    }

                    if (j == i && k != j) {
                        fn += matrix[j][k];
                    }

                    if (k == i && j != k) {
                        fp += matrix[j][k];
                    }
                }

                instancesPerClass[i] += matrix[i][j];
            }

            summary[i][0] = fp / (fp + tn); // fprate
            summary[i][1] = tp / (tp + fp); // precision
            summary[i][2] = tp / (tp + fn); // recall
            summary[i][3] = (2 * summary[i][1] * summary[i][2]) / (summary[i][1] + summary[i][2]); // f-measure
            summary[i][4] = i; // class
        }


        for (int i = 0; i < summary.length - 1; i++) {
            summary[summary.length - 1][0] += summary[i][0] * instancesPerClass[i];
            summary[summary.length - 1][1] += summary[i][1] * instancesPerClass[i];
            summary[summary.length - 1][2] += summary[i][2] * instancesPerClass[i];
            summary[summary.length - 1][3] += summary[i][3] * instancesPerClass[i];
        }
        summary[summary.length - 1][0] /= totalNumberOfInstances;
        summary[summary.length - 1][1] /= totalNumberOfInstances;
        summary[summary.length - 1][2] /= totalNumberOfInstances;
        summary[summary.length - 1][3] /= totalNumberOfInstances;


        System.out.println("FP Rate   Precision   Recall   F-Measure   Class");
        if (this.out != null) {
            out.println("FP Rate   Precision   Recall   F-Measure   Class");
        }

        String[] spaces = new String[]{"  ", "       ", "    ", "       ", "     "};

        DecimalFormat df = new DecimalFormat("0.000");
        for (int i = 0; i < summary.length; i++) {

            if (i == summary.length - 1) {
                System.out.println("-------------------------------------------------------");
                if (this.out != null) {
                    out.println("-------------------------------------------------------");
                }
            }

            for (int j = 0; j < summary[i].length; j++) {
                if (i == summary.length - 1 && j == summary[i].length - 1) {
                    System.out.print("   Weighted Avg.");
                    if (this.out != null) {
                        out.print("   Weighted Avg.");
                    }
                    continue;
                }

                if (j < summary[i].length - 1) {
                    System.out.print(spaces[j] + df.format(summary[i][j]).replace(",", "."));
                    if (this.out != null) {
                        out.print(spaces[j] + df.format(summary[i][j]).replace(",", "."));
                    }
                } else {
                    String label = ds.getLabel(i);
                    System.out.print("   " + String.valueOf(((char) (97 + summary[i][j]))) + " = " + label);
                    if (this.out != null) {
                        out.print("   " + String.valueOf(((char) (97 + summary[i][j]))) + " = " + label);
                    }
                }
            }

            System.out.println();
            if (this.out != null) {
                out.println();
            }
        }

        System.out.println();
        if (this.out != null) {
            out.println();
        }
    }

    public void printSummary() {
        int correct   = 0;
        int incorrect = 0;

        for (Result entry : this.results.keySet()) {
            int value = this.results.get(entry);

            if (entry.getPrediction() == value) {
                correct++;
            } else {
                incorrect++;
            }
        }

        DecimalFormat df = new DecimalFormat("#0.0000");

        String sCorrect    = String.valueOf(correct);
        String sCorrectPer = df.format((double) correct / (double) (correct + incorrect) * 100.0d) + " %";

        String sIncorrect    = String.valueOf(incorrect);
        String sIncorrectPer = df.format((double) incorrect / (double) (correct + incorrect) * 100.0d) + " %";

        String total = String.valueOf(correct + incorrect);

        while (sCorrect.length() < total.length()) {
            sCorrect = " " + sCorrect;
        }

        while (sIncorrect.length() < total.length()) {
            sIncorrect = " " + sIncorrect;
        }

        if (sCorrectPer.length() > sIncorrectPer.length()) {
            while (sCorrectPer.length() > sIncorrectPer.length()) {
                sIncorrectPer = " " + sIncorrectPer;
            }
        } else if (sCorrectPer.length() < sIncorrectPer.length()) {
            sCorrectPer = " " + sCorrectPer;
        }

        System.out.println("Correctly Classified Instances      " + sCorrect + "               " + sCorrectPer.replace(",", "."));
        System.out.println("Incorrectly Classified Instances    " + sIncorrect + "               " + sIncorrectPer.replace(",", "."));
        System.out.println("Total Number of Instances           " + total);
        System.out.println();
        if (this.out != null) {
            out.println("Correctly Classified Instances      " + sCorrect + "               " + sCorrectPer.replace(",", "."));
            out.println("Incorrectly Classified Instances    " + sIncorrect + "               " + sIncorrectPer.replace(",", "."));
            out.println("Total Number of Instances           " + total);
            out.println();
        }
    }
}
