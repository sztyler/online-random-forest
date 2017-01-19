package de.unima.classifiers.dataset;

import de.unima.classifiers.structure.Sample;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * ARFF (Attribute-Relation File Format). This module enables to load arff files that should be used for training or
 * testing.
 * More: http://www.cs.waikato.ac.nz/ml/weka/arff.html
 *
 * @author Timo Sztyler
 * @version 19.01.2017
 */
public class ARFF implements DataSet {
    private List<Sample>         samples;
    private Map<String, Integer> classLabels;
    private int                  numOfClasses;
    private int                  numOfFeatures;
    private int                  classIndex;
    private Set<Integer>         featureFilter;
    private boolean              featureInvert;
    private double[]             minFeatRange;
    private double[]             maxFeatRange;

    public ARFF() {
        this.samples = new ArrayList<>();
        this.classLabels = new HashMap<>();

        this.featureFilter = new HashSet<>();
        this.featureInvert = false;
    }

    @Override
    public void load(InputStream is, Map<String, Integer> classLabels) {
        this.classLabels = classLabels;
        this.load(is);
    }

    @Override
    public void load(InputStream is) {
        boolean flag = false;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            for (String line; (line = br.readLine()) != null; ) {
                if (line.length() == 0 || line.charAt(0) == '%') {
                    continue;
                }

                if (line.toUpperCase().startsWith("@ATTRIBUTE")) {
                    numOfFeatures++;
                }

                if (line.toUpperCase().startsWith("@ATTRIBUTE CLASS")) {
                    String   classes   = line.substring(line.indexOf("{") + 1, line.lastIndexOf("}"));
                    String[] fragments = classes.split(",");

                    this.numOfClasses = fragments.length;
                    for (String fragment : fragments) {
                        this.classLabels.put(fragment.trim(), classLabels.size());
                    }

                    this.classIndex = numOfFeatures - 1;
                    numOfFeatures--;
                }

                if (line.contains("@DATA")) {
                    flag = true;
                    this.numOfFeatures -= featureFilter.size();
                    if (this.featureInvert) {
                        this.numOfFeatures = featureFilter.size() - 1;
                    }

                    this.minFeatRange = new double[this.numOfFeatures];
                    this.maxFeatRange = new double[this.numOfFeatures];
                    continue;
                }

                if (!flag || line.length() == 0) {
                    continue;
                }

                String[] data       = line.split(",");
                double[] sampleData = new double[this.numOfFeatures];
                int      classID    = -1;
                int      cou        = 0;

                for (int i = 0; i < data.length; i++) {
                    if ((this.featureFilter.contains(i) && !this.featureInvert) || (!this.featureFilter.contains(i) && this.featureInvert)) {
                        continue;
                    }

                    if (this.classIndex != i) {
                        double value = Double.parseDouble(data[i]);
                        sampleData[cou] = value;
                    } else {
                        classID = this.classLabels.get(data[i]);
                        continue;
                    }

                    if (this.minFeatRange[cou] > sampleData[cou] || this.samples.size() == 0) {
                        this.minFeatRange[cou] = sampleData[cou];
                    }

                    if (this.maxFeatRange[cou] < sampleData[cou] || this.samples.size() == 0) {
                        this.maxFeatRange[cou] = sampleData[cou];
                    }

                    cou++;
                }

                Sample sample = new Sample(sampleData, classID, 1.0, samples.size());
                this.samples.add(sample);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getLabel(int id) {
        for (String key : classLabels.keySet()) {
            if (classLabels.get(key) == id) {
                return key;
            }
        }

        return null;
    }

    @Override
    public void randomize() {
        Collections.shuffle(this.samples);
        Collections.shuffle(this.samples);
        Collections.shuffle(this.samples);
    }

    @Override
    public void setFeatureFilter(Integer[] featureFilter, boolean invert) {
        this.featureFilter = new HashSet<>(Arrays.asList(featureFilter));
        this.featureInvert = invert;
    }

    @Override
    public List<Sample> getSamples() {
        return samples;
    }

    @Override
    public int getNumOfClasses() {
        return numOfClasses;
    }

    @Override
    public int getNumOfFeatures() {
        return numOfFeatures;
    }

    @Override
    public double[] getMinFeatRange() {
        return minFeatRange;
    }

    @Override
    public double[] getMaxFeatRange() {
        return maxFeatRange;
    }

    @Override
    public Map<String, Integer> getClassLabels() {
        return this.classLabels;
    }

    public String toLibSVM() {
        StringBuilder sb = new StringBuilder();

        String header = this.samples.size() + " " + this.numOfFeatures + " " + this.numOfClasses + " 1" + System.lineSeparator();
        sb.append(header);

        for (Sample sample : this.samples) {
            double[] values = sample.getValues();
            String   line   = String.valueOf(sample.getLabel());
            for (int i = 0; i < values.length - 1; i++) {
                line += " " + (i + 1) + ":" + values[i];
            }
            line += System.lineSeparator();

            sb.append(line);
        }

        return sb.toString();
    }

    public String[] toOMCLP() {
        StringBuilder data   = new StringBuilder();
        StringBuilder labels = new StringBuilder();

        String dataHeader = this.samples.size() + " " + this.numOfFeatures + System.lineSeparator();
        data.append(dataHeader);

        String labelsHeader = this.samples.size() + " " + 1 + System.lineSeparator();
        labels.append(labelsHeader);


        for (Sample sample : this.samples) {
            double[] values = sample.getValues();
            String   line   = "";

            for (int i = 0; i < values.length - 1; i++) {
                if (i != this.classIndex) {
                    line += values[i] + " ";
                }
            }
            line = line.trim();
            line += System.lineSeparator();

            labels.append(sample.getLabel()).append(System.lineSeparator());

            data.append(line);
        }

        return new String[]{data.toString(), labels.toString()};
    }
}