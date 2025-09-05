package wtf.uwu.utils.render.shader.impl;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import wtf.uwu.utils.InstanceAccess;
import wtf.uwu.utils.math.MathUtils;
import wtf.uwu.utils.render.RenderUtils;
import wtf.uwu.utils.render.shader.ShaderUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUniform1fv;

public class Shadow implements InstanceAccess {

    private static final int WEIGHT_BUFFER_SIZE = 256;
    private static final int TEXTURE_UNIT_SOURCE = 0;
    private static final int TEXTURE_UNIT_CHECK = 16;

    public static ShaderUtils bloomShader = new ShaderUtils("shadow");
    public static Framebuffer bloomFramebuffer = new Framebuffer(1, 1, false);
    public static float prevRadius;

    public static void renderBloom(int sourceTexture, int radius, int offset) {
        // Preparar framebuffer y estados OpenGL
        setupRenderState();

        // Primera pasada: renderizar a framebuffer intermedio
        renderFirstPass(sourceTexture, radius, offset);

        // Segunda pasada: combinar con textura original
        renderSecondPass(sourceTexture, radius, offset);

        // Limpiar estado
        cleanupRenderState();
    }

    private static void setupRenderState() {
        bloomFramebuffer = RenderUtils.createFrameBuffer(bloomFramebuffer, false);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.0f);
        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL_GREATER, 0);
    }

    private static void renderFirstPass(int sourceTexture, int radius, int offset) {
        bloomFramebuffer.framebufferClear();
        bloomFramebuffer.bindFramebuffer(true);

        bloomShader.init();
        setupUniforms(radius, offset, 0);
        glBindTexture(GL_TEXTURE_2D, sourceTexture);
        ShaderUtils.drawQuads();
        bloomShader.unload();

        bloomFramebuffer.unbindFramebuffer();
    }

    private static void renderSecondPass(int sourceTexture, int radius, int offset) {
        mc.getFramebuffer().bindFramebuffer(true);

        bloomShader.init();
        setupUniforms(radius, 0, offset);

        // Configurar múltiples texturas
        GL13.glActiveTexture(GL13.GL_TEXTURE16);
        glBindTexture(GL_TEXTURE_2D, sourceTexture);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, bloomFramebuffer.framebufferTexture);

        ShaderUtils.drawQuads();
        bloomShader.unload();
    }

    private static void cleanupRenderState() {
        GlStateManager.alphaFunc(516, 0.1f);
        GlStateManager.enableAlpha();
        GlStateManager.bindTexture(0);
    }

    public static void setupUniforms(int radius, int directionX, int directionY) {
        // Solo recalcular pesos si el radio cambió
        if (radius != prevRadius) {
            updateGaussianWeights(radius);
            prevRadius = radius;
        }

        // Configurar uniforms que cambian en cada frame
        setFrameDependentUniforms(directionX, directionY);
    }

    private static void updateGaussianWeights(int radius) {
        final FloatBuffer weightBuffer = BufferUtils.createFloatBuffer(WEIGHT_BUFFER_SIZE);

        // Calcular pesos gaussianos
        for (int i = 0; i <= radius; i++) {
            weightBuffer.put(MathUtils.calculateGaussianValue(i, radius));
        }
        weightBuffer.rewind();

        // Configurar uniforms estáticos
        bloomShader.setUniformi("inTexture", TEXTURE_UNIT_SOURCE);
        bloomShader.setUniformi("textureToCheck", TEXTURE_UNIT_CHECK);
        bloomShader.setUniformf("radius", radius);
        glUniform1fv(bloomShader.getUniform("weights"), weightBuffer);
    }

    private static void setFrameDependentUniforms(int directionX, int directionY) {
        bloomShader.setUniformf("texelSize",
                1.0F / (float) mc.displayWidth,
                1.0F / (float) mc.displayHeight);
        bloomShader.setUniformf("direction", directionX, directionY);
    }
}