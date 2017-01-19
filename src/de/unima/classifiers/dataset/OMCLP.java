package de.unima.classifiers.dataset;

import de.unima.classifiers.structure.Sample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.*;

/**
 * OMCLP is a modification of the common LibSVM format. This module enables to load OMCLP files that should be used for
 * training or testing. This file format was used by the original implementation.
 * More: https://github.com/amirsaffari/online-multiclass-lpboost
 *
 * @author Timo Sztyler
 * @version 19.01.2017
 */
public class OMCLP implements DataSet {
    private List<Sample>         samples;
    private Map<String, Integer> classLabels;
    private int                  numOfClasses;
    private int                  numOfFeatures;
    private Set<Integer>         featureFilter;
    private double[]             minFeatRange;
    private double[]             maxFeatRange;

    public OMCLP() {
        this.samples = new ArrayList<>();
        this.numOfFeatures = -1;

        this.featureFilter = new HashSet<>();
    }

    @Override
    public void load(InputStream is, Map<String, Integer> classLabels) {
        // TODO
    }

    @Override
    public void load(InputStream is) {
        // TODO
    }

    @Deprecated
    public void load(String path, Map<String, Integer> classLabels) {
        this.classLabels = classLabels;
        this.load(path);
    }

    @Deprecated
    public void load(String path) {
        File dataSet = new File(path + ".structure");
        File labels  = new File(path + ".labels");

        List<String> fullLabelList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(labels))) {
            br.readLine(); // skip first line
            for (String line; (line = br.readLine()) != null; ) {
                if (this.classLabels == null) {
                    this.classLabels = new HashMap<>();
                }

                String label = line.trim();
                fullLabelList.add(label);

                if (!this.classLabels.containsKey(label)) {
                    this.classLabels.put(label, classLabels.size());
                }
            }

            this.numOfClasses = this.classLabels.size();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (BufferedReader br = new BufferedReader(new FileReader(dataSet))) {
            for (String line; (line = br.readLine()) != null; ) {
                String[] fragments = line.split(" ");

                if (fragments.length == 2) {
                    this.numOfFeatures = Integer.parseInt(fragments[1]);
                    this.numOfFeatures -= this.featureFilter.size();

                    this.minFeatRange = new double[this.numOfFeatures];
                    this.maxFeatRange = new double[this.numOfFeatures];

                    continue;
                }

                double[] sampleData = new double[this.numOfFeatures];
                int      classID    = this.classLabels.get(fullLabelList.get(samples.size()));
                int      cou        = 0;

                for (int i = 0; i < fragments.length; i++) {
                    if (this.featureFilter.contains(i)) {
                        continue;
                    }

                    double value = Double.parseDouble(fragments[i]);
                    sampleData[cou] = value;

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
}