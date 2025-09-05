package wtf.uwu.features.modules.impl.visual;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import wtf.uwu.UwU;
import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.render.Render3DEvent;
import wtf.uwu.features.friend.Friend;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.features.modules.impl.combat.KillAura;
import wtf.uwu.features.values.impl.BoolValue;
import wtf.uwu.features.values.impl.ColorValue;
import wtf.uwu.features.values.impl.ModeValue;
import wtf.uwu.features.values.impl.SliderValue;
import wtf.uwu.utils.math.MathUtils;
import wtf.uwu.utils.render.ColorUtils;
import wtf.uwu.utils.render.GLUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "Cosmetics", category = ModuleCategory.Visual)
public class Cosmetics extends Module {

    // General Settings
    public final BoolValue target = new BoolValue("Target", true, this);
    public final BoolValue friends = new BoolValue("Friends", true, this);

    // Hat Settings
    public final BoolValue hatEnabled = new BoolValue("Hat", true, this);
    public final ModeValue hatMode = new ModeValue("Hat Mode", new String[]{"Astolfo", "Sexy", "Fade", "Dynamic", "Rainbow"}, "Sexy", this, () -> hatEnabled.get());
    public final SliderValue hatPoints = new SliderValue("Hat Points", 30, 3, 180, this, () -> hatEnabled.get());
    public final SliderValue hatSize = new SliderValue("Hat Size", 0.5f, 0.1f, 3.0f, 0.1f, this, () -> hatEnabled.get());

    // Wings Settings
    public final BoolValue wingsEnabled = new BoolValue("Wings", false, this);
    public final ModeValue wingType = new ModeValue("Wing Type", new String[]{"Angel", "Dragon", "Butterfly", "Demon", "Phoenix"}, "Angel", this, () -> wingsEnabled.get());
    public final SliderValue wingSize = new SliderValue("Wing Size", 1.0f, 0.3f, 2.5f, 0.1f, this, () -> wingsEnabled.get());
    public final SliderValue wingSpeed = new SliderValue("Wing Speed", 1.0f, 0.1f, 3.0f, 0.1f, this, () -> wingsEnabled.get());
    public final BoolValue wingAnimation = new BoolValue("Wing Animation", true, this, () -> wingsEnabled.get());

    // Halo Settings
    public final BoolValue haloEnabled = new BoolValue("Halo", false, this);
    public final ModeValue haloType = new ModeValue("Halo Type", new String[]{"Classic", "Spinning", "Pulsing", "Double"}, "Classic", this, () -> haloEnabled.get());
    public final SliderValue haloSize = new SliderValue("Halo Size", 0.8f, 0.3f, 2.0f, 0.1f, this, () -> haloEnabled.get());
    public final SliderValue haloHeight = new SliderValue("Halo Height", 0.5f, 0.0f, 1.5f, 0.1f, this, () -> haloEnabled.get());

    // Particle Effects
    public final BoolValue particlesEnabled = new BoolValue("Particles", false, this);
    public final ModeValue particleType = new ModeValue("Particle Type", new String[]{"Hearts", "Stars", "Sparkles", "Fire", "Magic", "Snow"}, "Hearts", this, () -> particlesEnabled.get());
    public final SliderValue particleAmount = new SliderValue("Particle Amount", 10, 1, 50, this, () -> particlesEnabled.get());
    public final SliderValue particleSpeed = new SliderValue("Particle Speed", 1.0f, 0.1f, 3.0f, 0.1f, this, () -> particlesEnabled.get());

    // Aura Settings
    public final BoolValue auraEnabled = new BoolValue("Aura", false, this);
    public final ModeValue auraType = new ModeValue("Aura Type", new String[]{"Energy", "Divine", "Dark", "Electric", "Flame"}, "Energy", this, () -> auraEnabled.get());
    public final SliderValue auraRadius = new SliderValue("Aura Radius", 1.5f, 0.5f, 3.0f, 0.1f, this, () -> auraEnabled.get());
    public final SliderValue auraIntensity = new SliderValue("Aura Intensity", 0.7f, 0.1f, 1.0f, 0.1f, this, () -> auraEnabled.get());

    // Color Settings
    public final ColorValue primaryColor = new ColorValue("Primary Color", new Color(255, 255, 255), this);
    public final ColorValue secondaryColor = new ColorValue("Secondary Color", new Color(0, 0, 0), this);
    private final SliderValue colorOffset = new SliderValue("Color Offset", 2000.0f, 0.0f, 5000.0f, 100.0f, this);

    // Internal variables
    private final double[][] hatPositions = new double[181][2];
    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();
    private int lastHatPoints;
    private double lastHatSize;
    private long lastParticleSpawn;

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        // Update hat calculations if needed
        if (hatEnabled.get() && (this.lastHatSize != this.hatSize.get() || this.lastHatPoints != this.hatPoints.get())) {
            this.lastHatSize = this.hatSize.get();
            this.computeHatPoints(this.lastHatPoints = (int) this.hatPoints.get(), this.lastHatSize);
        }

        // Update particles
        if (particlesEnabled.get()) {
            updateParticles();
        }

        // Render cosmetics for player
        renderCosmetics(event, mc.thePlayer);

        // Render cosmetics for friends
        if (friends.get() && !UwU.INSTANCE.getFriendManager().getFriends().isEmpty()) {
            for (Friend friend : UwU.INSTANCE.getFriendManager().getFriends()) {
                EntityPlayer friendPlayer = mc.theWorld.getPlayerEntityByName(friend.getUsername());
                if (friendPlayer != null) {
                    renderCosmetics(event, friendPlayer);
                }
            }
        }

        // Render cosmetics for targets
        if (target.get() && !getModule(KillAura.class).targets.isEmpty()) {
            for (EntityLivingBase target : getModule(KillAura.class).targets) {
                if (target instanceof EntityPlayer) {
                    renderCosmetics(event, (EntityPlayer) target);
                }
            }
        }
    }

    private void renderCosmetics(Render3DEvent event, EntityPlayer player) {
        if (player == null || (player == mc.thePlayer && mc.gameSettings.thirdPersonView == 0)) {
            return;
        }

        setupGL();

        final float partialTicks = event.partialTicks();
        final RenderManager render = mc.getRenderManager();

        glTranslated(-render.renderPosX, -render.renderPosY, -render.renderPosZ);

        final double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        final double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        final double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        glPushMatrix();
        glTranslated(x, y, z);

        // Adjust for sneaking
        if (player.isSneaking()) {
            glTranslated(0, -0.2, 0);
        }

        // Apply player rotation
        glRotatef(MathUtils.interpolate(player.prevRotationYawHead, player.rotationYawHead, partialTicks), 0, -1, 0);

        // Render each cosmetic type
        if (hatEnabled.get()) renderHat(partialTicks, player);
        if (wingsEnabled.get()) renderWings(partialTicks, player);
        if (haloEnabled.get()) renderHalo(partialTicks, player);
        if (auraEnabled.get()) renderAura(partialTicks, player);
        if (particlesEnabled.get()) renderParticles(partialTicks, player, x, y, z);

        glPopMatrix();
        glTranslated(render.renderPosX, render.renderPosY, render.renderPosZ);

        restoreGL();
    }

    private void renderHat(float partialTicks, EntityPlayer player) {
        glPushMatrix();
        glTranslated(0, 1.9, 0);

        final float pitch = MathUtils.interpolate(player.prevRotationPitchHead, player.rotationPitchHead, partialTicks);
        glRotatef(pitch / 3.f, 1, 0, 0);
        glTranslated(0, 0, pitch / 270.f);

        final int points = (int) hatPoints.get();
        Color[] colors = generateColors(points + 1);

        // Render hat outline
        glEnable(GL_LINE_SMOOTH);
        glLineWidth(2.0f);
        glBegin(GL_LINE_LOOP);
        addCircleVertices(points - 1, colors, 255);
        glEnd();
        glDisable(GL_LINE_SMOOTH);

        // Render hat cone
        glBegin(GL_TRIANGLE_FAN);
        glVertex3d(0, hatSize.get() / 2, 0);
        addCircleVertices(points, colors, 128);
        glEnd();

        glPopMatrix();
    }

    private void renderWings(float partialTicks, EntityPlayer player) {
        glPushMatrix();
        glTranslated(0, 1.4, 0);

        final float wingAnimOffset = wingAnimation.get() ?
                (float) Math.sin(System.currentTimeMillis() * wingSpeed.get() / 500.0) * 0.3f : 0;

        // Left wing
        glPushMatrix();
        glTranslated(-0.3, 0, 0.2);
        glRotatef(15 + wingAnimOffset * 20, 0, 0, 1);
        renderWing(wingType.get(), wingSize.get(), false);
        glPopMatrix();

        // Right wing
        glPushMatrix();
        glTranslated(0.3, 0, 0.2);
        glRotatef(-15 - wingAnimOffset * 20, 0, 0, 1);
        renderWing(wingType.get(), wingSize.get(), true);
        glPopMatrix();

        glPopMatrix();
    }

    private void renderWing(String type, float size, boolean right) {
        Color wingColor = primaryColor.get();
        glColor4f(wingColor.getRed() / 255f, wingColor.getGreen() / 255f, wingColor.getBlue() / 255f, 0.8f);

        switch (type) {
            case "Angel":
                renderAngelWing(size, right);
                break;
            case "Dragon":
                renderDragonWing(size, right);
                break;
            case "Butterfly":
                renderButterflyWing(size, right);
                break;
            case "Demon":
                renderDemonWing(size, right);
                break;
            case "Phoenix":
                renderPhoenixWing(size, right);
                break;
        }
    }

    private void renderAngelWing(float size, boolean right) {
        float modifier = right ? 1 : -1;

        glBegin(GL_TRIANGLE_FAN);
        glVertex3f(0, 0, 0);

        // Wing outline points
        float[][] wingPoints = {
                {0.1f * modifier, 0.8f * size, 0},
                {0.3f * modifier, 0.9f * size, -0.1f},
                {0.6f * modifier, 0.7f * size, -0.2f},
                {0.8f * modifier, 0.4f * size, -0.1f},
                {0.9f * modifier, 0.1f * size, 0},
                {0.7f * modifier, -0.2f * size, 0.1f},
                {0.4f * modifier, -0.3f * size, 0.1f},
                {0.1f * modifier, -0.2f * size, 0}
        };

        for (float[] point : wingPoints) {
            glVertex3f(point[0], point[1], point[2]);
        }

        glEnd();
    }

    private void renderDragonWing(float size, boolean right) {
        float modifier = right ? 1 : -1;
        Color darkColor = new Color(primaryColor.get().getRed() / 2, primaryColor.get().getGreen() / 2, primaryColor.get().getBlue() / 2);
        glColor4f(darkColor.getRed() / 255f, darkColor.getGreen() / 255f, darkColor.getBlue() / 255f, 0.9f);

        // More angular, bat-like wing
        glBegin(GL_TRIANGLE_FAN);
        glVertex3f(0, 0, 0);

        float[][] wingPoints = {
                {0.2f * modifier, 0.9f * size, -0.1f},
                {0.5f * modifier, 0.8f * size, -0.3f},
                {0.8f * modifier, 0.5f * size, -0.2f},
                {1.0f * modifier, 0.2f * size, -0.1f},
                {0.9f * modifier, -0.1f * size, 0.1f},
                {0.6f * modifier, -0.4f * size, 0.2f},
                {0.3f * modifier, -0.3f * size, 0.1f}
        };

        for (float[] point : wingPoints) {
            glVertex3f(point[0], point[1], point[2]);
        }

        glEnd();
    }

    private void renderButterflyWing(float size, boolean right) {
        float modifier = right ? 1 : -1;

        // Colorful butterfly wing with gradient
        glBegin(GL_TRIANGLE_FAN);
        glVertex3f(0, 0, 0);

        Color[] butterflyColors = {primaryColor.get(), secondaryColor.get(), primaryColor.get()};

        float[][] wingPoints = {
                {0.1f * modifier, 0.6f * size, 0},
                {0.4f * modifier, 0.8f * size, -0.05f},
                {0.6f * modifier, 0.6f * size, -0.1f},
                {0.5f * modifier, 0.3f * size, -0.05f},
                {0.7f * modifier, 0.1f * size, -0.05f},
                {0.6f * modifier, -0.2f * size, 0},
                {0.3f * modifier, -0.1f * size, 0.05f}
        };

        for (int i = 0; i < wingPoints.length; i++) {
            Color color = butterflyColors[i % butterflyColors.length];
            glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 0.7f);
            glVertex3f(wingPoints[i][0], wingPoints[i][1], wingPoints[i][2]);
        }

        glEnd();
    }

    private void renderDemonWing(float size, boolean right) {
        float modifier = right ? 1 : -1;
        glColor4f(0.2f, 0.0f, 0.0f, 0.9f); // Dark red

        // Torn, demonic wing
        glBegin(GL_TRIANGLE_FAN);
        glVertex3f(0, 0, 0);

        float[][] wingPoints = {
                {0.15f * modifier, 0.8f * size, -0.1f},
                {0.4f * modifier, 0.9f * size, -0.2f},
                {0.6f * modifier, 0.7f * size, -0.3f},
                {0.8f * modifier, 0.4f * size, -0.2f},
                {0.9f * modifier, 0.0f * size, -0.1f},
                {0.7f * modifier, -0.3f * size, 0.1f},
                {0.4f * modifier, -0.4f * size, 0.15f},
                {0.2f * modifier, -0.2f * size, 0.1f}
        };

        for (float[] point : wingPoints) {
            glVertex3f(point[0], point[1], point[2]);
        }

        glEnd();
    }

    private void renderPhoenixWing(float size, boolean right) {
        float modifier = right ? 1 : -1;

        // Fire-like gradient colors
        Color[] fireColors = {
                new Color(255, 100, 0), // Orange
                new Color(255, 150, 0), // Yellow-orange
                new Color(255, 200, 0), // Yellow
                new Color(255, 100, 0)  // Back to orange
        };

        glBegin(GL_TRIANGLE_FAN);
        glVertex3f(0, 0, 0);

        float[][] wingPoints = {
                {0.1f * modifier, 0.9f * size, 0},
                {0.3f * modifier, 1.0f * size, -0.1f},
                {0.6f * modifier, 0.8f * size, -0.15f},
                {0.9f * modifier, 0.5f * size, -0.1f},
                {1.0f * modifier, 0.1f * size, 0},
                {0.8f * modifier, -0.2f * size, 0.1f},
                {0.5f * modifier, -0.3f * size, 0.1f},
                {0.2f * modifier, -0.2f * size, 0.05f}
        };

        for (int i = 0; i < wingPoints.length; i++) {
            Color color = fireColors[i % fireColors.length];
            glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 0.8f);
            glVertex3f(wingPoints[i][0], wingPoints[i][1], wingPoints[i][2]);
        }

        glEnd();
    }

    private void renderHalo(float partialTicks, EntityPlayer player) {
        glPushMatrix();
        glTranslated(0, 2.3 + haloHeight.get(), 0);

        float rotation = 0;
        if (haloType.is("Spinning")) {
            rotation = (System.currentTimeMillis() % 3600) / 10f;
            glRotatef(rotation, 0, 1, 0);
        }

        Color haloColor = primaryColor.get();
        glColor4f(haloColor.getRed() / 255f, haloColor.getGreen() / 255f, haloColor.getBlue() / 255f, 0.8f);

        float size = haloSize.get();
        if (haloType.is("Pulsing")) {
            size *= 1 + Math.sin(System.currentTimeMillis() / 300.0) * 0.2;
        }

        // Main halo ring
        glLineWidth(3.0f);
        glBegin(GL_LINE_LOOP);
        for (int i = 0; i < 32; i++) {
            double angle = i * Math.PI * 2 / 32;
            glVertex3d(Math.cos(angle) * size, 0, Math.sin(angle) * size);
        }
        glEnd();

        // Double halo
        if (haloType.is("Double")) {
            glLineWidth(2.0f);
            glColor4f(haloColor.getRed() / 255f, haloColor.getGreen() / 255f, haloColor.getBlue() / 255f, 0.5f);
            glBegin(GL_LINE_LOOP);
            for (int i = 0; i < 24; i++) {
                double angle = i * Math.PI * 2 / 24;
                glVertex3d(Math.cos(angle) * size * 0.7, 0.1, Math.sin(angle) * size * 0.7);
            }
            glEnd();
        }

        glPopMatrix();
    }

    private void renderAura(float partialTicks, EntityPlayer player) {
        Color auraColor = primaryColor.get();
        float alpha = auraIntensity.get() * 0.3f;

        glColor4f(auraColor.getRed() / 255f, auraColor.getGreen() / 255f, auraColor.getBlue() / 255f, alpha);

        float radius = auraRadius.get();

        switch (auraType.get()) {
            case "Energy":
                renderEnergyAura(radius);
                break;
            case "Divine":
                renderDivineAura(radius);
                break;
            case "Dark":
                renderDarkAura(radius);
                break;
            case "Electric":
                renderElectricAura(radius);
                break;
            case "Flame":
                renderFlameAura(radius);
                break;
        }
    }

    private void renderEnergyAura(float radius) {
        glPushMatrix();
        glTranslated(0, 1, 0);

        // Spinning energy rings
        for (int ring = 0; ring < 3; ring++) {
            glPushMatrix();
            glRotatef((System.currentTimeMillis() % 3600) / (10f + ring * 5), 0, 1, 0);
            glRotatef(ring * 60, 1, 0, 0);

            glLineWidth(1.5f);
            glBegin(GL_LINE_LOOP);
            for (int i = 0; i < 16; i++) {
                double angle = i * Math.PI * 2 / 16;
                glVertex3d(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
            }
            glEnd();

            glPopMatrix();
        }

        glPopMatrix();
    }

    private void renderDivineAura(float radius) {
        glColor4f(1.0f, 1.0f, 0.8f, auraIntensity.get() * 0.4f);

        // Gentle pulsing light
        float pulse = (float) (1 + Math.sin(System.currentTimeMillis() / 500.0) * 0.3);

        glPushMatrix();
        glTranslated(0, 1, 0);
        glScalef(pulse, pulse, pulse);

        glBegin(GL_TRIANGLE_FAN);
        glVertex3d(0, 0, 0);
        for (int i = 0; i <= 32; i++) {
            double angle = i * Math.PI * 2 / 32;
            glVertex3d(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
        }
        glEnd();

        glPopMatrix();
    }

    private void renderDarkAura(float radius) {
        glColor4f(0.2f, 0.0f, 0.2f, auraIntensity.get() * 0.5f);

        // Dark swirling energy
        glPushMatrix();
        glTranslated(0, 1, 0);

        for (int layer = 0; layer < 4; layer++) {
            glPushMatrix();
            glRotatef((System.currentTimeMillis() % 7200) / (20f - layer * 2), 0, 1, 0);

            glBegin(GL_TRIANGLE_STRIP);
            for (int i = 0; i <= 16; i++) {
                double angle = i * Math.PI * 2 / 16;
                double innerRadius = radius * (0.3 + layer * 0.2);
                double outerRadius = radius * (0.5 + layer * 0.2);

                glVertex3d(Math.cos(angle) * innerRadius, layer * 0.1, Math.sin(angle) * innerRadius);
                glVertex3d(Math.cos(angle) * outerRadius, layer * 0.1, Math.sin(angle) * outerRadius);
            }
            glEnd();

            glPopMatrix();
        }

        glPopMatrix();
    }

    private void renderElectricAura(float radius) {
        glColor4f(0.5f, 0.8f, 1.0f, auraIntensity.get() * 0.6f);

        // Electric bolts
        glLineWidth(2.0f);
        glBegin(GL_LINES);

        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI * 2 / 8;
            double startX = Math.cos(angle) * 0.3;
            double startZ = Math.sin(angle) * 0.3;
            double endX = Math.cos(angle) * radius;
            double endZ = Math.sin(angle) * radius;

            // Jagged electric bolt
            glVertex3d(startX, 1, startZ);
            glVertex3d(endX + random.nextGaussian() * 0.1, 1 + random.nextGaussian() * 0.2, endZ + random.nextGaussian() * 0.1);
        }

        glEnd();
    }

    private void renderFlameAura(float radius) {
        // Fire-like colors
        Color[] fireColors = {
                new Color(255, 100, 0, (int)(auraIntensity.get() * 100)),
                new Color(255, 150, 0, (int)(auraIntensity.get() * 80)),
                new Color(255, 200, 0, (int)(auraIntensity.get() * 60))
        };

        glPushMatrix();
        glTranslated(0, 0.5, 0);

        for (int flame = 0; flame < 12; flame++) {
            double angle = flame * Math.PI * 2 / 12;
            double flameX = Math.cos(angle) * radius * 0.8;
            double flameZ = Math.sin(angle) * radius * 0.8;

            glPushMatrix();
            glTranslated(flameX, 0, flameZ);

            Color flameColor = fireColors[flame % fireColors.length];
            glColor4f(flameColor.getRed() / 255f, flameColor.getGreen() / 255f, flameColor.getBlue() / 255f, flameColor.getAlpha() / 255f);

            // Flickering flame shape
            float flicker = (float) Math.sin(System.currentTimeMillis() / 100.0 + flame) * 0.1f + 1.0f;

            glBegin(GL_TRIANGLE_FAN);
            glVertex3f(0, 0, 0);
            glVertex3f(-0.05f, 0.3f * flicker, 0);
            glVertex3f(0, 0.5f * flicker, 0);
            glVertex3f(0.05f, 0.3f * flicker, 0);
            glEnd();

            glPopMatrix();
        }

        glPopMatrix();
    }

    private void updateParticles() {
        long currentTime = System.currentTimeMillis();

        // Spawn new particles
        if (currentTime - lastParticleSpawn > 100) { // Spawn every 100ms
            lastParticleSpawn = currentTime;

            if (particles.size() < particleAmount.get()) {
                particles.add(new Particle(
                        (random.nextDouble() - 0.5) * 2,
                        random.nextDouble() * 2,
                        (random.nextDouble() - 0.5) * 2,
                        particleType.get()
                ));
            }
        }

        // Update existing particles
        particles.removeIf(particle -> {
            particle.update(particleSpeed.get());
            return particle.isDead();
        });
    }

    private void renderParticles(float partialTicks, EntityPlayer player, double x, double y, double z) {
        for (Particle particle : particles) {
            particle.render(x, y, z, primaryColor.get());
        }
    }

    // Utility methods
    private void computeHatPoints(final int points, final double radius) {
        for (int i = 0; i <= points; i++) {
            final double circleX = radius * StrictMath.cos(i * Math.PI * 2 / points);
            final double circleZ = radius * StrictMath.sin(i * Math.PI * 2 / points);
            this.hatPositions[i][0] = circleX;
            this.hatPositions[i][1] = circleZ;
        }
    }

    private void addCircleVertices(final int points, final Color[] colors, final int alpha) {
        for (int i = 0; i <= points; i++) {
            final double[] pos = this.hatPositions[i];
            final Color clr = colors[i];
            GL11.glColor4f(clr.getRed() / 255.0f, clr.getGreen() / 255.0f, clr.getBlue() / 255.0f, alpha / 255.0f);
            glVertex3d(pos[0], 0, pos[1]);
        }
    }

    private Color[] generateColors(int count) {
        Color[] colors = new Color[count];
        Color[] baseColors = getBaseColors();

        for (int i = 0; i < count; i++) {
            colors[i] = fadeBetween(baseColors, colorOffset.get(), i * (colorOffset.get() / count));
        }

        return colors;
    }

    private Color[] getBaseColors() {
        return switch (hatMode.get()) {
            case "Astolfo" -> new Color[]{
                    new Color(252, 106, 140), new Color(252, 106, 213),
                    new Color(218, 106, 252), new Color(145, 106, 252),
                    new Color(106, 140, 252), new Color(106, 213, 252)
            };
            case "Sexy" -> new Color[]{
                    new Color(255, 150, 255), new Color(255, 132, 199),
                    new Color(211, 101, 187), new Color(160, 80, 158),
                    new Color(120, 63, 160), new Color(123, 65, 168)
            };
            case "Fade" -> new Color[]{primaryColor.get(), secondaryColor.get()};
            case "Dynamic" -> new Color[]{primaryColor.get(), new Color(ColorUtils.darker(primaryColor.get().getRGB(), 0.25F))};
            case "Rainbow" -> new Color[]{
                    Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA
            };
            default -> new Color[]{primaryColor.get()};
        };
    }

    private Color fadeBetween(final Color[] table, final double speed, final double offset) {
        return this.fadeBetween(table, (System.currentTimeMillis() + offset) % speed / speed);
    }

    private Color fadeBetween(final Color[] table, final double progress) {
        final int i = table.length;
        if (progress == 1.0) return table[0];
        if (progress == 0.0) return table[i - 1];

        final double max = Math.max(0.0, (1.0 - progress) * (i - 1));
        final int min = (int) max;
        return this.fadeBetween(table[min], table[min + 1], max - min);
    }

    private Color fadeBetween(final Color start, final Color end, double progress) {
        if (progress > 1.0) {
            progress = 1.0 - progress % 1.0;
        }
        return this.gradient(start, end, progress);
    }

    private Color gradient(final Color start, final Color end, final double progress) {
        final double invert = 1.0 - progress;
        return new Color(
                (int) (start.getRed() * invert + end.getRed() * progress),
                (int) (start.getGreen() * invert + end.getGreen() * progress),
                (int) (start.getBlue() * invert + end.getBlue() * progress),
                (int) (start.getAlpha() * invert + end.getAlpha() * progress)
        );
    }

    private void setupGL() {
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_CULL_FACE);
        glDepthMask(false);
        glDisable(GL_DEPTH_TEST);
        glShadeModel(GL_SMOOTH);
        GLUtils.startBlend();
    }

    private void restoreGL() {
        GLUtils.endBlend();
        glDepthMask(true);
        glShadeModel(GL_FLAT);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glEnable(GL_TEXTURE_2D);
    }

    // Particle class for particle effects
    private static class Particle {
        private double x, y, z;
        private final double motionX, motionY, motionZ;
        private final String type;
        private int age;
        private final int maxAge;

        public Particle(double x, double y, double z, String type) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.type = type;
            this.age = 0;
            this.maxAge = 60 + (int)(Math.random() * 40); // 1-5 seconds at 20 TPS

            Random rand = new Random();
            this.motionX = (rand.nextDouble() - 0.5) * 0.1;
            this.motionY = rand.nextDouble() * 0.05 + 0.02;
            this.motionZ = (rand.nextDouble() - 0.5) * 0.1;
        }

        public void update(float speed) {
            x += motionX * speed;
            y += motionY * speed;
            z += motionZ * speed;
            age++;
        }

        public boolean isDead() {
            return age >= maxAge;
        }

        public void render(double playerX, double playerY, double playerZ, Color color) {
            glPushMatrix();

            double renderX = playerX + x;
            double renderY = playerY + y + 1;
            double renderZ = playerZ + z;

            glTranslated(renderX, renderY, renderZ);

            // Face the player
            glRotatef(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
            glRotatef(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);

            float alpha = 1.0f - (float) age / maxAge;
            glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, alpha);

            float size = 0.1f * (1.0f - (float) age / maxAge * 0.5f);

            switch (type) {
                case "Hearts" -> renderHeart(size);
                case "Stars" -> renderStar(size);
                case "Sparkles" -> renderSparkle(size);
                case "Fire" -> renderFireParticle(size);
                case "Magic" -> renderMagicParticle(size);
                case "Snow" -> renderSnowflake(size);
            }

            glPopMatrix();
        }

        private void renderHeart(float size) {
            glColor4f(1.0f, 0.4f, 0.6f, 0.8f);
            glBegin(GL_TRIANGLES);
            // Simple heart shape using triangles
            glVertex3f(0, size, 0);
            glVertex3f(-size/2, size/2, 0);
            glVertex3f(size/2, size/2, 0);

            glVertex3f(0, 0, 0);
            glVertex3f(-size/2, size/2, 0);
            glVertex3f(size/2, size/2, 0);
            glEnd();
        }

        private void renderStar(float size) {
            glColor4f(1.0f, 1.0f, 0.4f, 0.9f);
            glBegin(GL_TRIANGLE_FAN);
            glVertex3f(0, 0, 0);
            for (int i = 0; i <= 10; i++) {
                float angle = i * (float) Math.PI * 2 / 10;
                float radius = (i % 2 == 0) ? size : size / 2;
                glVertex3f((float) Math.cos(angle) * radius, (float) Math.sin(angle) * radius, 0);
            }
            glEnd();
        }

        private void renderSparkle(float size) {
            glBegin(GL_LINES);
            glVertex3f(-size, 0, 0);
            glVertex3f(size, 0, 0);
            glVertex3f(0, -size, 0);
            glVertex3f(0, size, 0);
            glVertex3f(0, 0, -size);
            glVertex3f(0, 0, size);
            glEnd();
        }

        private void renderFireParticle(float size) {
            glColor4f(1.0f, 0.5f, 0.0f, 0.7f);
            glBegin(GL_TRIANGLE_FAN);
            glVertex3f(0, 0, 0);
            for (int i = 0; i <= 8; i++) {
                float angle = i * (float) Math.PI * 2 / 8;
                glVertex3f((float) Math.cos(angle) * size, (float) Math.sin(angle) * size, 0);
            }
            glEnd();
        }

        private void renderMagicParticle(float size) {
            glColor4f(0.7f, 0.4f, 1.0f, 0.8f);
            glBegin(GL_QUADS);
            glVertex3f(-size, -size, 0);
            glVertex3f(size, -size, 0);
            glVertex3f(size, size, 0);
            glVertex3f(-size, size, 0);
            glEnd();
        }

        private void renderSnowflake(float size) {
            glColor4f(1.0f, 1.0f, 1.0f, 0.9f);
            glBegin(GL_LINES);
            // Main cross
            glVertex3f(-size, 0, 0);
            glVertex3f(size, 0, 0);
            glVertex3f(0, -size, 0);
            glVertex3f(0, size, 0);
            // Diagonal lines
            glVertex3f(-size/2, -size/2, 0);
            glVertex3f(size/2, size/2, 0);
            glVertex3f(-size/2, size/2, 0);
            glVertex3f(size/2, -size/2, 0);
            glEnd();
        }
    }
}