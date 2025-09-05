package net.optifine.util;

import net.minecraft.util.MathHelper;

public class MathUtils
{
    public static final float PI = (float)Math.PI;
    public static final float PI2 = ((float)Math.PI * 2F);
    public static final float PId2 = ((float)Math.PI / 2F);

    // Lookup tables expandidas para mejor precisión y performance
    private static final int ASIN_TABLE_SIZE = 131072; // 2^17 para mejor precisión
    private static final int TRIG_TABLE_SIZE = 65536;
    private static final float[] ASIN_TABLE = new float[ASIN_TABLE_SIZE];
    private static final float[] SIN_TABLE = new float[TRIG_TABLE_SIZE];
    private static final float[] COS_TABLE = new float[TRIG_TABLE_SIZE];

    // Constantes para conversiones rápidas
    private static final float ASIN_TABLE_FACTOR = (ASIN_TABLE_SIZE - 1) / 2.0F;
    private static final float TRIG_TABLE_FACTOR = TRIG_TABLE_SIZE / PI2;
    private static final int TRIG_TABLE_MASK = TRIG_TABLE_SIZE - 1;

    // Funciones trigonométricas optimizadas con lookup tables
    public static float asin(float value)
    {
        int index = (int)((value + 1.0F) * ASIN_TABLE_FACTOR) & (ASIN_TABLE_SIZE - 1);
        return ASIN_TABLE[index];
    }

    public static float acos(float value)
    {
        int index = (int)((value + 1.0F) * ASIN_TABLE_FACTOR) & (ASIN_TABLE_SIZE - 1);
        return PId2 - ASIN_TABLE[index];
    }

    // Nuevas funciones sin y cos rápidas
    public static float fastSin(float angle)
    {
        int index = (int)(angle * TRIG_TABLE_FACTOR) & TRIG_TABLE_MASK;
        return SIN_TABLE[index];
    }

    public static float fastCos(float angle)
    {
        int index = (int)(angle * TRIG_TABLE_FACTOR) & TRIG_TABLE_MASK;
        return COS_TABLE[index];
    }

    // Función sqrt rápida usando aproximación de Newton-Raphson
    public static float fastSqrt(float x)
    {
        if (x <= 0.0F) return 0.0F;

        // Aproximación inicial usando bit manipulation
        int i = Float.floatToIntBits(x);
        i = 0x1FBD1DF5 + (i >> 1);
        x = Float.intBitsToFloat(i);

        // Una iteración de Newton-Raphson para mayor precisión
        return 0.5F * (x + (Float.floatToIntBits(x) / x));
    }

    public static float fastInverseSqrt(float x)
    {
        float xhalf = 0.5F * x;
        int i = Float.floatToIntBits(x);
        i = 0x5f3759df - (i >> 1);
        x = Float.intBitsToFloat(i);
        x = x * (1.5F - xhalf * x * x);
        return x;
    }

    public static int getAverage(int[] vals)
    {
        int len = vals.length;
        if (len <= 0) return 0;

        return getSum(vals) / len;
    }

    public static int getSum(int[] vals)
    {
        int len = vals.length;
        if (len <= 0) return 0;

        int sum = 0;
        int i = 0;

        for (; i < len - 7; i += 8) {
            sum += vals[i] + vals[i+1] + vals[i+2] + vals[i+3] +
                    vals[i+4] + vals[i+5] + vals[i+6] + vals[i+7];
        }

        for (; i < len; i++) {
            sum += vals[i];
        }

        return sum;
    }

    public static int roundDownToPowerOfTwo(int val)
    {
        if (val <= 0) return 0;
        return Integer.highestOneBit(val);
    }

    public static boolean isPowerOfTwo(int val)
    {
        return val > 0 && (val & (val - 1)) == 0;
    }

    public static int nextPowerOfTwo(int val)
    {
        if (val <= 0) return 1;
        if (isPowerOfTwo(val)) return val;
        return Integer.highestOneBit(val) << 1;
    }

    public static boolean equalsDelta(float f1, float f2, float delta)
    {
        float diff = f1 - f2;
        return diff >= -delta && diff <= delta; // Evita Math.abs
    }

    public static boolean isZero(float value, float epsilon)
    {
        return value >= -epsilon && value <= epsilon;
    }

    public static float toDeg(float angle)
    {
        return angle * 57.29577951F;
    }

    public static float toRad(float angle)
    {
        return angle * 0.017453292F;
    }

    public static float normalizeAngle(float angle)
    {
        angle %= PI2;
        if (angle < 0) angle += PI2;
        return angle;
    }

    public static float roundToFloat(double d)
    {
        return (float)((long)(d * 1.0E8D + 0.5D) * 1.0E-8D);
    }

    public static int fastFloor(float value)
    {
        int i = (int)value;
        return value < i ? i - 1 : i;
    }

    public static int fastCeil(float value)
    {
        int i = (int)value;
        return value > i ? i + 1 : i;
    }

    public static float clamp(float value, float min, float max)
    {
        return value < min ? min : (value > max ? max : value);
    }

    public static int clamp(int value, int min, int max)
    {
        return value < min ? min : (value > max ? max : value);
    }

    public static float lerp(float a, float b, float t)
    {
        return a + t * (b - a);
    }

    public static float smoothStep(float edge0, float edge1, float x)
    {
        float t = clamp((x - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    static
    {
        for (int i = 0; i < ASIN_TABLE_SIZE; ++i)
        {
            ASIN_TABLE[i] = (float)Math.asin((double)i / ASIN_TABLE_FACTOR - 1.0D);
        }

        ASIN_TABLE[0] = (float)Math.asin(-1.0D);
        ASIN_TABLE[ASIN_TABLE_SIZE/2] = (float)Math.asin(0.0D);
        ASIN_TABLE[ASIN_TABLE_SIZE-1] = (float)Math.asin(1.0D);

        for (int i = 0; i < TRIG_TABLE_SIZE; ++i)
        {
            float angle = (float)i / TRIG_TABLE_FACTOR;
            SIN_TABLE[i] = (float)Math.sin(angle);
            COS_TABLE[i] = (float)Math.cos(angle);
        }
    }
}