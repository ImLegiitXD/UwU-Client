package wtf.uwu.utils.render.shader.impl;

import net.minecraft.client.gui.ScaledResolution;
import wtf.uwu.utils.InstanceAccess;
import wtf.uwu.utils.render.shader.ShaderUtils;

public class MainMenu implements InstanceAccess {
    private static final ShaderUtils mainmenuShader = new ShaderUtils("mainmenu");

    public static void draw(long initialTime) {
        ScaledResolution scaledRes = new ScaledResolution(mc);
        mainmenuShader.init();
        mainmenuShader.setUniformf("TIME", (float) (System.currentTimeMillis() - initialTime) / 1000.0f);
        mainmenuShader.setUniformf("RESOLUTION",
                (float) (scaledRes.getScaledWidth() * scaledRes.getScaleFactor()),
                (float) (scaledRes.getScaledHeight() * scaledRes.getScaleFactor())
        );
        ShaderUtils.drawFixedQuads();
        mainmenuShader.unload();
    }

    public static void drawWithCustomization(long initialTime) {
        ScaledResolution scaledRes = new ScaledResolution(mc);
        mainmenuShader.init();

        // Tiempo con velocidad personalizable
        float timeSpeed = 0.5f; // Cambia esto para velocidad (0.1f = muy lento, 2.0f = muy rápido)
        mainmenuShader.setUniformf("TIME", (float) (System.currentTimeMillis() - initialTime) / 1000.0f * timeSpeed);

        // Resolución
        mainmenuShader.setUniformf("RESOLUTION",
                (float) (scaledRes.getScaledWidth() * scaledRes.getScaleFactor()),
                (float) (scaledRes.getScaledHeight() * scaledRes.getScaleFactor())
        );

        // PARÁMETROS ADICIONALES QUE PODRÍAS AGREGAR (si el shader los soporta):

        // Colores personalizados (RGB values 0.0 to 1.0)
        mainmenuShader.setUniformf("COLOR1", 0.8f, 0.2f, 0.8f); // Purple
        mainmenuShader.setUniformf("COLOR2", 0.2f, 0.8f, 0.9f); // Cyan

        // Intensidad del efecto
        mainmenuShader.setUniformf("INTENSITY", 1.0f);

        // Escala del patrón
        mainmenuShader.setUniformf("SCALE", 1.0f);

        // Posición del mouse (para efectos interactivos)
        // mainmenuShader.setUniformf("MOUSE", mouseX / (float)width, mouseY / (float)height);

        ShaderUtils.drawFixedQuads();
        mainmenuShader.unload();
    }

    // VERSIÓN CON DIFERENTES PRESETS
    public static void drawWithPreset(long initialTime, BackgroundPreset preset) {
        ScaledResolution scaledRes = new ScaledResolution(mc);
        mainmenuShader.init();

        // Aplicar configuración del preset
        mainmenuShader.setUniformf("TIME", (float) (System.currentTimeMillis() - initialTime) / 1000.0f * preset.speed);
        mainmenuShader.setUniformf("RESOLUTION",
                (float) (scaledRes.getScaledWidth() * scaledRes.getScaleFactor()),
                (float) (scaledRes.getScaledHeight() * scaledRes.getScaleFactor())
        );

        // Si el shader soporta estos uniforms:
        mainmenuShader.setUniformf("COLOR1", preset.color1[0], preset.color1[1], preset.color1[2]);
        mainmenuShader.setUniformf("COLOR2", preset.color2[0], preset.color2[1], preset.color2[2]);
        mainmenuShader.setUniformf("INTENSITY", preset.intensity);

        ShaderUtils.drawFixedQuads();
        mainmenuShader.unload();
    }

    // Enum para diferentes presets de fondo
    public enum BackgroundPreset {
        ORIGINAL(1.0f, new float[]{0.5f, 0.0f, 1.0f}, new float[]{0.0f, 0.5f, 1.0f}, 1.0f),
        FIRE(1.5f, new float[]{1.0f, 0.3f, 0.0f}, new float[]{1.0f, 0.8f, 0.0f}, 1.2f),
        OCEAN(0.7f, new float[]{0.0f, 0.4f, 0.8f}, new float[]{0.0f, 0.8f, 0.6f}, 0.8f),
        PURPLE_HAZE(0.8f, new float[]{0.6f, 0.0f, 0.8f}, new float[]{0.9f, 0.0f, 0.5f}, 1.0f),
        MATRIX(2.0f, new float[]{0.0f, 1.0f, 0.0f}, new float[]{0.0f, 0.5f, 0.0f}, 1.5f),
        SUNSET(0.6f, new float[]{1.0f, 0.5f, 0.0f}, new float[]{1.0f, 0.0f, 0.5f}, 0.9f);

        public final float speed;
        public final float[] color1;
        public final float[] color2;
        public final float intensity;

        BackgroundPreset(float speed, float[] color1, float[] color2, float intensity) {
            this.speed = speed;
            this.color1 = color1;
            this.color2 = color2;
            this.intensity = intensity;
        }
    }
}