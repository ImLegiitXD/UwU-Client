package wtf.uwu.features.modules.impl.visual.island;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;
import wtf.uwu.UwU;
import wtf.uwu.features.modules.impl.movement.Scaffold;
import wtf.uwu.features.modules.impl.visual.Interface;
import wtf.uwu.gui.font.FontRenderer;
import wtf.uwu.gui.font.Fonts;
import wtf.uwu.gui.notification.Notification;
import wtf.uwu.utils.InstanceAccess;
import wtf.uwu.utils.animations.Animation;
import wtf.uwu.utils.animations.ContinualAnimation;
import wtf.uwu.utils.animations.Direction;
import wtf.uwu.utils.render.RenderUtils;
import wtf.uwu.utils.render.RoundedUtils;

import java.awt.*;
import java.util.Deque;

@Getter
@Setter
public class IslandRenderer implements InstanceAccess {

    public static IslandRenderer INSTANCE = new IslandRenderer();

    public ContinualAnimation animatedX = new ContinualAnimation();
    public ContinualAnimation animatedY = new ContinualAnimation();
    public ContinualAnimation widthAnimation = new ContinualAnimation();
    public ContinualAnimation heightAnimation = new ContinualAnimation();

    public float x, y, width, height;
    private ScaledResolution sr;

    public FontRenderer largest = Fonts.interBold.get(20);
    public FontRenderer titleFont = Fonts.interMedium.get(18);
    public FontRenderer medium = Fonts.interMedium.get(15);
    public FontRenderer small = Fonts.interRegular.get(10);

    public String title = "";
    public String description = "";
    private IslandMode currentMode = IslandMode.DEFAULT;

    private static final int ANIMATION_SPEED = 40;
    private static final int DEFAULT_Y_POSITION = 40;
    private static final int PADDING = 10;
    private static final int INNER_PADDING = 5;
    private static final int PROGRESS_BAR_HEIGHT = 5;
    private static final float PROGRESS_BAR_RADIUS = 2.5f;
    private static final int CORNER_RADIUS = 7;

    private static final Color PROGRESS_BAR_BACKGROUND = new Color(255, 255, 255, 80);
    private static final Color PROGRESS_BAR_FOREGROUND = new Color(255, 255, 255, 255);

    public IslandRenderer() {
        this.sr = new ScaledResolution(mc);
        initializeDefaultState();
    }

    private void initializeDefaultState() {
        if (mc.theWorld == null) {
            resetToDefault();
        }
    }

    private void resetToDefault() {
        x = sr.getScaledWidth() / 2f;
        y = DEFAULT_Y_POSITION;
        width = 0;
        height = 0;
        title = "";
        description = "";
        currentMode = IslandMode.DEFAULT;
    }

    public void render(ScaledResolution sr, boolean shader) {
        this.sr = sr;

        if (mc.theWorld == null) {
            resetToDefault();
            return;
        }

        if (shouldRenderScaffold()) {
            renderScaffoldMode(shader);
        } else if (shouldRenderNotifications()) {
            renderNotificationMode(shader);
        } else {
            renderDefaultMode(shader);
        }
    }

    private boolean shouldRenderScaffold() {
        Scaffold scaffold = UwU.INSTANCE.getModuleManager().getModule(Scaffold.class);
        return scaffold.isEnabled() && scaffold.getBlockCount() > 0;
    }

    private boolean shouldRenderNotifications() {
        return !UwU.INSTANCE.getNotificationManager().getNotifications().isEmpty();
    }

    private void renderScaffoldMode(boolean shader) {
        currentMode = IslandMode.SCAFFOLD;
        Scaffold scaffold = UwU.INSTANCE.getModuleManager().getModule(Scaffold.class);
        int blockCount = scaffold.getBlockCount();

        title = "Block Counter";
        description = buildBlockCountDescription(blockCount);

        calculateDimensions(30);
        setPosition(sr.getScaledWidth() / 2f, DEFAULT_Y_POSITION);

        prepareRendering();
        drawBackgroundAuto(1);
        drawScaffoldProgressBar(blockCount);
        drawText(shader);
        finishRendering();
    }

    private void renderNotificationMode(boolean shader) {
        currentMode = IslandMode.NOTIFICATION;
        Deque<Notification> notifications = UwU.INSTANCE.getNotificationManager().getNotifications();
        boolean isExhi = UwU.INSTANCE.getModuleManager().getModule(Interface.class).notificationMode.is("Exhi");

        cleanupNotifications(notifications, isExhi);

        if (notifications.isEmpty()) {
            renderDefaultMode(shader);
            return;
        }

        Notification notification = notifications.getLast();
        Animation animation = notification.getAnimation();

        if (!animation.finished(Direction.BACKWARDS)) {
            title = notification.getTitle();
            description = notification.getDescription();

            calculateDimensions(30);
            setPosition(sr.getScaledWidth() / 2f, DEFAULT_Y_POSITION);

            prepareRendering();
            drawBackgroundAuto(1);
            drawNotificationProgressBar(notification);
            drawText(shader);
            finishRendering();
        }
    }

    private void renderDefaultMode(boolean shader) {
        currentMode = IslandMode.DEFAULT;

        title = buildDefaultTitle();
        description = "";

        calculateDimensions(15);
        setPosition(sr.getScaledWidth() / 2f, DEFAULT_Y_POSITION);

        GL11.glPushMatrix();
        prepareRendering();
        drawBackgroundAuto(0);
        drawDefaultText(shader);
        finishRendering();
        GL11.glPopMatrix();
    }

    private String buildBlockCountDescription(int blockCount) {
        EnumChatFormatting color = getBlockCountColor(blockCount);
        return "Stack Size: " + color + blockCount;
    }

    private EnumChatFormatting getBlockCountColor(int blockCount) {
        if (blockCount > 64) return EnumChatFormatting.GREEN;
        if (blockCount > 32) return EnumChatFormatting.YELLOW;
        return EnumChatFormatting.RED;
    }

    private String buildDefaultTitle() {
        return "UwU" + EnumChatFormatting.WHITE + " | " +
                mc.thePlayer.getName() + " | " +
                Minecraft.getDebugFPS() + " FPS";
    }

    private void cleanupNotifications(Deque<Notification> notifications, boolean isExhi) {
        notifications.removeIf(notification -> {
            Animation animation = notification.getAnimation();
            boolean timeElapsed = notification.getTimerUtils().hasTimeElapsed((long) notification.getTime());
            animation.setDirection(timeElapsed ? Direction.BACKWARDS : Direction.FORWARDS);
            return !isExhi && animation.finished(Direction.BACKWARDS);
        });
    }

    private void calculateDimensions(int targetHeight) {
        float titleWidth = (currentMode == IslandMode.DEFAULT) ?
                titleFont.getStringWidth(title) : largest.getStringWidth(title);
        float descWidth = description.isEmpty() ? 0 : medium.getStringWidth(description);

        float targetWidth = Math.max(descWidth, titleWidth + PADDING) + PADDING;

        widthAnimation.animate(targetWidth, ANIMATION_SPEED);
        heightAnimation.animate(targetHeight, ANIMATION_SPEED);

        width = widthAnimation.getOutput();
        height = heightAnimation.getOutput();
    }

    private void setPosition(float targetX, float targetY) {
        x = targetX;
        y = targetY;
        runToXy(x, y);
    }

    private void prepareRendering() {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
    }

    private void finishRendering() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private void drawScaffoldProgressBar(int blockCount) {
        float progressBarY = animatedY.getOutput() + ((y - animatedY.getOutput()) * 2);
        float progressBarWidth = width - 12;
        float progressBarX = animatedX.getOutput() + 6;

        RoundedUtils.drawRound(progressBarX, progressBarY, progressBarWidth,
                PROGRESS_BAR_HEIGHT, PROGRESS_BAR_RADIUS, PROGRESS_BAR_BACKGROUND);

        float progress = Math.min(64, blockCount) / 64f;
        RoundedUtils.drawRound(progressBarX, progressBarY, progressBarWidth * progress,
                PROGRESS_BAR_HEIGHT, PROGRESS_BAR_RADIUS, PROGRESS_BAR_FOREGROUND);
    }

    private void drawNotificationProgressBar(Notification notification) {
        float progressBarY = animatedY.getOutput() + ((y - animatedY.getOutput()) * 2);
        float progressBarWidth = width - 12;
        float progressBarX = animatedX.getOutput() + 6;

        RoundedUtils.drawRound(progressBarX, progressBarY, progressBarWidth,
                PROGRESS_BAR_HEIGHT, PROGRESS_BAR_RADIUS, PROGRESS_BAR_BACKGROUND);

        float progress = Math.min(notification.getTimerUtils().getTime() / notification.getTime(), 1f);
        RoundedUtils.drawRound(progressBarX, progressBarY, progressBarWidth * progress,
                PROGRESS_BAR_HEIGHT, PROGRESS_BAR_RADIUS, PROGRESS_BAR_FOREGROUND);
    }

    private void drawText(boolean shader) {
        if (!shader) {
            float titleX = animatedX.getOutput() + INNER_PADDING;
            float titleY = animatedY.getOutput() + 6;
            float descY = animatedY.getOutput() + 18;

            largest.drawString(title, titleX, titleY, -1);
            if (!description.isEmpty()) {
                medium.drawString(description, titleX, descY, -1);
            }
        }
    }

    private void drawDefaultText(boolean shader) {
        if (!shader) {
            float titleX = animatedX.getOutput() + INNER_PADDING;
            float titleY = animatedY.getOutput() + INNER_PADDING;
            int color = UwU.INSTANCE.getModuleManager().getModule(Interface.class).color();

            titleFont.drawString(title, titleX, titleY, color);
        }
    }

    public float getRenderX(float x) {
        return x - width / 2;
    }

    public float getRenderY(float y) {
        return y - height / 2;
    }

    public void runToXy(float realX, float realY) {
        animatedX.animate(getRenderX(realX), ANIMATION_SPEED);
        animatedY.animate(getRenderY(realY), ANIMATION_SPEED);
    }

    public void drawBackgroundAuto(int identifier) {
        float renderHeight = ((y - animatedY.getOutput()) * 2) + (identifier == 1 ? PADDING : 0);
        float renderWidth = (x - animatedX.getOutput()) * 2;

        RenderUtils.scissor(
                animatedX.getOutput() - 1,
                animatedY.getOutput() - 1,
                renderWidth + 2,
                renderHeight + 2
        );

        Interface interfaceModule = UwU.INSTANCE.getModuleManager().getModule(Interface.class);
        Color backgroundColor = new Color(interfaceModule.bgColor(), true);

        RoundedUtils.drawRound(
                animatedX.getOutput(),
                animatedY.getOutput(),
                renderWidth,
                renderHeight,
                CORNER_RADIUS,
                backgroundColor
        );
    }

    private enum IslandMode {
        DEFAULT,
        SCAFFOLD,
        NOTIFICATION
    }
}