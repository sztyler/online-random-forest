# Online Random Forest Classifier (Java)
This is a Java implementation of an online random forest classifier. Online machine learning enables to update an existing classification model continuously without keeping the processed data available or knowing all data a-priori. This implementation relies on the implementation of [Amir Saffari](https://github.com/amirsaffari/online-random-forests) (C++). We reimplemented this classifier to use it also on wearable devices, e.g., android platforms.

## Releases
Download: [latest build](https://github.com/sztyler/online-random-forest/releases/download/Build290916/online-random-forest-b290916.jar) (JAR, Build290916)

## Usage
```java
DataSet data = new ARFF();
File file = new File("data.arff");
InputStream is = new FileInputStream(file);
data.load(is);
RandomForest rf = new RandomForest(...)

for (int nEpoch = 0; nEpoch < config.numEpochs; nEpoch++) { // train
  data.randomize();
  for (Sample sample : arffTrain.getSamples()) {
    rf.update(sample);
  }
}

for (Sample sample : arffTest.getSamples()) { // test
  Result result = new Result(data.getNumOfClasses());
  rf.eval(sample, result);
  results.put(result, sample.getLabel());
}
```
[Please also consider the complete example](https://github.com/sztyler/online-random-forest/blob/master/src/de/unima/classifiers/example/ExampleUsage.java)

## Documentation
**Documentation (JavaDoc) is coming soon** . If you have any issues, feel free to [contact me](http://sensor.informatik.uni-mannheim.de)
