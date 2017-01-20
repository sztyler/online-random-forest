# Online Random Forest Classifier (Java)
This is a Java implementation of an online random forest classifier. Online machine learning enables to update an existing classification model continuously without keeping the processed data available or knowing all data a-priori. This implementation relies on the implementation of [Amir Saffari](https://github.com/amirsaffari/online-random-forests) (C++). We reimplemented this classifier to use it also on wearable devices, e.g., android platforms.

## Releases
Download: [latest build](https://github.com/sztyler/online-random-forest/releases/tag/Build190117) (JAR, Build190117)

## Usage
```java
DataSet dsTrain = new ARFF();
dsTrain.load(new FileInputStream(new File("data/data_train.arff")));
DataSet dsTest = new ARFF();
dsTest.load(new FileInputStream(new File("data/data_test.arff")));

Config config = new Config();
RandomForest rf = new RandomForest(config, dsTrain.getNumOfClasses(), dsTrain.getNumOfFeatures(), dsTrain.getMinFeatRange(), dsTrain.getMaxFeatRange());

for (int nEpoch = 0; nEpoch < config.numEpochs; nEpoch++) { // train
  dsTrain.randomize();
  for (Sample sample : dsTrain.getSamples()) {
    rf.update(sample);
  }
}

for (Sample sample : dsTest.getSamples()) { // test
  Result result = new Result(dsTrain.getNumOfClasses());
  rf.eval(sample, result);
  results.put(result, sample.getLabel());
}
```
[Please also consider the complete example](https://github.com/sztyler/online-random-forest/blob/master/src/de/unima/classifiers/example/ExampleUsage.java)

## Documentation
**Documentation (JavaDoc) is coming soon** . If you have any issues, feel free to [contact me](http://sensor.informatik.uni-mannheim.de)
