package wtf.uwu.utils.render;

import wtf.uwu.UwU;
import java.awt.*;

public class AnimatedBackgroundUtils {

    private static final float[] SIN_CACHE = new float[360];
    private static final float[] COS_CACHE = new float[360];

    static {
        for (int i = 0; i < 360; i++) {
            SIN_CACHE[i] = (float) Math.sin(Math.toRadians(i));
            COS_CACHE[i] = (float) Math.cos(Math.toRadians(i));
        }
    }

    private static int[] particleColors = new int[3];
    private static boolean colorsInitialized = false;

    public static void drawMinimalAnimatedBackground(int width, int height) {
        long startTime = UwU.INSTANCE.getStartTimeLong();
        float time = (startTime % 20000) / 1000.0f;

        float centerX = width * 0.5f;
        float centerY = height * 0.5f;

        int timeIndex1 = (int)((time * 0.3f * 57.2958f) % 360);
        int timeIndex2 = (int)((time * 0.2f * 57.2958f) % 360);

        float colorWave1 = fastSin(timeIndex1) * 0.5f + 0.5f;
        float colorWave2 = fastCos(timeIndex2) * 0.5f + 0.5f;

        int topColor = calculateTopColor(colorWave1, colorWave2);
        int bottomColor = calculateBottomColor(colorWave1, colorWave2);

        RenderUtils.drawGradientRect(0.0, 0.0, (double)width, (double)height, true, topColor, bottomColor);

        if (!colorsInitialized) {
            initializeParticleColors();
            colorsInitialized = true;
        }

        drawAnimatedParticles(time, width, height, centerX, centerY);

        drawAnimatedCornerAccents(time, width, height);

        drawPulsingCenterGlow(time, width, height, centerX, centerY);

        drawMovingWaves(time, width, height);
    }

    private static float fastSin(int degrees) {
        return SIN_CACHE[degrees % 360];
    }

    private static float fastCos(int degrees) {
        return COS_CACHE[degrees % 360];
    }

    private static int calculateTopColor(float colorWave1, float colorWave2) {
        int r = clampColor((int)(8 + colorWave1 * 15));
        int g = clampColor((int)(12 + colorWave2 * 20));
        int b = clampColor((int)(20 + colorWave1 * 25));
        return (255 << 24) | (r << 16) | (g << 8) | b;
    }

    private static int calculateBottomColor(float colorWave1, float colorWave2) {
        int r = clampColor((int)(15 + colorWave2 * 10));
        int g = clampColor((int)(18 + colorWave1 * 15));
        int b = clampColor((int)(25 + colorWave2 * 20));
        return (255 << 24) | (r << 16) | (g << 8) | b;
    }

    private static void initializeParticleColors() {
        particleColors[0] = (255 << 24) | (120 << 16) | (160 << 8) | 220;
        particleColors[1] = (255 << 24) | (100 << 16) | (140 << 8) | 200;
        particleColors[2] = (255 << 24) | (140 << 16) | (180 << 8) | 240;
    }

    private static int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static int clampAlpha(float alpha) {
        return Math.max(0, Math.min(255, (int)(alpha * 255)));
    }

    private static void drawAnimatedParticles(float time, int width, int height, float centerX, float centerY) {
        float widthFactor = width * 0.45f;
        float heightFactor = height * 0.35f;

        for (int i = 0; i < 15; i++) {
            float speedMultiplier = 0.8f + (i % 3) * 0.2f;
            int indexX = (int)((time * speedMultiplier * 0.4f + i * 1.2f) * 57.2958f) % 360;
            int indexY = (int)((time * speedMultiplier * 0.3f + i * 0.9f) * 57.2958f) % 360;

            float offsetX = fastSin(indexX) * widthFactor;
            float offsetY = fastCos(indexY) * heightFactor;

            float x = centerX + offsetX;
            float y = centerY + offsetY;

            float pulseSpeed = 0.6f + (i % 4) * 0.3f;
            int pulseIndex = (int)((time * pulseSpeed + i) * 57.2958f) % 360;
            float alphaBase = 0.1f + 0.15f * fastSin(pulseIndex);
            float alpha = Math.max(0.0f, Math.min(1.0f, alphaBase));

            int sizeIndex = (int)((time * pulseSpeed * 1.5f + i) * 57.2958f) % 360;
            float size = Math.max(1.0f, 1.5f + 1.0f * fastSin(sizeIndex));

            int colorVariation = i % 3;
            int baseColor = particleColors[colorVariation];
            int alphaInt = clampAlpha(alpha);
            int particleColor = (baseColor & 0x00FFFFFF) | (alphaInt << 24);

            RenderUtils.drawRoundedRect((int)(x - size), (int)(y - size), (int)(size * 2), (int)(size * 2), (int)size, particleColor);
        }
    }

    private static void drawAnimatedCornerAccents(float time, int width, int height) {
        int pulseIndex = (int)((time * 0.8f) * 57.2958f) % 360;
        float pulse = Math.max(0.2f, 0.6f + 0.4f * fastSin(pulseIndex));
        int alpha = clampAlpha(0.3f * pulse);

        int accentColor = (alpha << 24) | (60 << 16) | (90 << 8) | 150;

        int lengthIndex = (int)((time * 0.5f) * 57.2958f) % 360;
        float lineLength = Math.max(40, 60 + 20 * fastSin(lengthIndex));

        int intLineLength = (int)lineLength;
        int widthMinusLength = (int)(width - lineLength);
        int heightMinusLength = (int)(height - lineLength);

        RenderUtils.drawRect(0, 0, intLineLength, 1, accentColor);
        RenderUtils.drawRect(0, 0, 1, intLineLength, accentColor);

        RenderUtils.drawRect(widthMinusLength, 0, intLineLength, 1, accentColor);
        RenderUtils.drawRect(width - 1, 0, 1, intLineLength, accentColor);

        RenderUtils.drawRect(0, height - 1, intLineLength, 1, accentColor);
        RenderUtils.drawRect(0, heightMinusLength, 1, intLineLength, accentColor);

        RenderUtils.drawRect(widthMinusLength, height - 1, intLineLength, 1, accentColor);
        RenderUtils.drawRect(width - 1, heightMinusLength, 1, intLineLength, accentColor);
    }

    private static void drawPulsingCenterGlow(float time, int width, int height, float centerX, float centerY) {
        int pulseIndex = (int)((time * 0.6f) * 57.2958f) % 360;
        float pulse = Math.max(0.4f, 0.7f + 0.3f * fastSin(pulseIndex));

        float baseAlpha = 0.025f * pulse;
        float alphaDecrement = 0.003f * pulse;

        for (int i = 0; i < 6; i++) {
            float radius = Math.max(10, (80 + (i * 30)) * pulse);
            float alpha = Math.max(0.0f, baseAlpha - (i * alphaDecrement));

            int alphaInt = clampAlpha(alpha);
            int glowColor = (alphaInt << 24) | (80 << 16) | (120 << 8) | 180;

            int intRadius = (int)radius;
            RenderUtils.drawRoundedRect((int)(centerX - radius), (int)(centerY - radius),
                    intRadius * 2, intRadius * 2, intRadius, glowColor);
        }
    }

    private static void drawMovingWaves(float time, int width, int height) {
        float heightFactor = height * 0.3f;
        float baseY = height * 0.2f;

        for (int i = 0; i < 3; i++) {
            int waveIndex = (int)((time * 0.4f + i * 2.0f) * 57.2958f) % 360;
            float waveY = baseY + i * heightFactor + fastSin(waveIndex) * 50;

            float alpha = Math.max(0.0f, 0.03f - (i * 0.008f));
            int alphaInt = clampAlpha(alpha);
            int waveColor = (alphaInt << 24) | (100 << 16) | (140 << 8) | 200;

            float timeWave = time * 0.8f;
            for (int x = 0; x < width; x += 4) {
                int heightIndex = (int)((x * 0.01f + timeWave) * 57.2958f) % 360;
                float waveHeight = Math.max(1, 2 + fastSin(heightIndex) * 1);
                RenderUtils.drawRect(x, (int)waveY, 4, (int)waveHeight, waveColor);
            }
        }
    }
}