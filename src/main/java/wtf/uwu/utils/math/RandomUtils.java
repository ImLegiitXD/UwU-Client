package wtf.uwu.utils.math;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {

    private static final Random RANDOM = new Random();
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    public static int randomInt(int min, int max) {
        return RANDOM.nextInt((max - min) + 1) + min;
    }
    public static double randomDouble(double min, double max) {
        return min + (max - min) * RANDOM.nextDouble();
    }
    public static boolean randomBoolean() {
        return RANDOM.nextBoolean();
    }
    public static int threadLocalInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
    public static double threadLocalDouble(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    public static boolean threadLocalBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }


    public static int secureInt(int min, int max) {
        return SECURE_RANDOM.nextInt((max - min) + 1) + min;
    }

    public static double secureDouble(double min, double max) {
        return min + (max - min) * SECURE_RANDOM.nextDouble();
    }

    public static float secureFloat(float min, float max) {
        return min + (max - min) * SECURE_RANDOM.nextFloat();
    }

    public static boolean secureBoolean() {
        return SECURE_RANDOM.nextBoolean();
    }

    public static double gaussian(double mean, double standardDeviation) {
        return mean + RANDOM.nextGaussian() * standardDeviation;
    }


    public static double jitter(double base, double range) {
        return base + randomDouble(-range, range);
    }


    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static boolean chance(double probability) {
        return RANDOM.nextDouble() < probability;
    }

    public static <T> T randomElement(T[] array) {
        if (array == null || array.length == 0) return null;
        return array[RANDOM.nextInt(array.length)];
    }


    public static <T> T randomElement(java.util.List<T> list) {
        if (list == null || list.isEmpty()) return null;
        return list.get(RANDOM.nextInt(list.size()));
    }
}
