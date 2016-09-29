package de.unima.classifiers;

import java.io.File;
import java.io.FileInputStream;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 * This class provides some basic functions, e.g., generating random values.
 *
 * @author Timo Sztyler
 * @version 29.09.2016
 */
public class Utilities {
    public static int poisson(double A) {
        int k    = 0;
        int maxK = 10;

        while (true) {
            double U_k = randDouble();
            A *= U_k;
            if (k > maxK || A < 0.36787944117) {    // exp(-1.0) = 0.36787944117
                break;
            }
            k++;
        }

        return k;
    }

    public static double randDouble(double minRange, double maxRange) {
        return (minRange + (maxRange - minRange) * Utilities.randDouble());
    }


    public static int getMaxCoeffIndex(double[] d) {
        double value = d[0];
        int    index = 0;

        for (int i = 0; i < d.length; i++) {
            if (d[i] > value) {
                value = d[i];
                index = i;
            }
        }

        return index;
    }

    public static double getSum(double[] values) {
        double sum = 0.0;

        for (double value : values) {
            sum += value;
        }

        return sum;
    }

    private static double randDouble() {
        long tv_sec  = System.currentTimeMillis() / 1000L;
        long tv_usec = System.nanoTime();
        // long pid = Utilities.getPID();
        // long uRandom = Utilities.getDevRandom();

        Random random = new Random(tv_sec * tv_usec); // + pid + uRandom
        //Random random = new Random();

        return random.nextDouble();
    }

    private static long getPID() {
        String name = ManagementFactory.getRuntimeMXBean().getName();

        if (name == null || !name.contains("@")) {
            return 1;
        }

        return Long.parseLong(name.substring(0, name.indexOf("@")));
    }

    private static long getDevRandom() {
        int  outInt     = 0;
        File urandom    = new File("/dev/urandom");
        byte tempByte[] = new byte[Integer.BYTES];

        try {
            FileInputStream fis = new FileInputStream(urandom);
            fis.read(tempByte, 0, Integer.BYTES);
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
            buffer.put(tempByte);
            buffer.flip();
            outInt = buffer.getInt();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return outInt;
    }

    public static double[] calcWeights(int[] absolute, int[] tree) {
        double[] result = new double[absolute.length];

        for (int i = 0; i < result.length; i++) {
            result[i] = 1.0 - ((double) tree[i] / (double) absolute[i]);
        }

        return result;
    }
}