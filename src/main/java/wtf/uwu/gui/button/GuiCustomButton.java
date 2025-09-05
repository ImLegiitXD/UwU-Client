package wtf.uwu.gui.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wtf.uwu.gui.font.FontRenderer;
import wtf.uwu.gui.font.Fonts;
import wtf.uwu.utils.animations.Animation;
import wtf.uwu.utils.animations.Direction;
import wtf.uwu.utils.animations.impl.SmoothStepAnimation;
import wtf.uwu.utils.render.MouseUtils;
import wtf.uwu.utils.render.RoundedUtils;

import java.awt.*;

public class GuiCustomButton extends GuiButton {

    public final Animation hoverAnimation;
    public final Animation clickAnimation;
    public final Animation disabledAnimation;

    public FontRenderer fontRenderer;
    public float radius;
    public Runnable clickAction;

    public ColorTheme colorTheme;

    public boolean isAnimating = false;
    public long lastClickTime = 0;

    public static final int DEFAULT_ANIMATION_DURATION = 400;
    public static final int CLICK_ANIMATION_DURATION = 150;
    public static final float CLICK_SCALE_REDUCTION = 0.05f;
    public static final float DISABLED_ALPHA_MULTIPLIER = 0.5f;


    public enum ColorTheme {
        DARK(new Color(0, 0, 0, 128), new Color(40, 40, 40, 180), new Color(60, 60, 60, 200),
                Color.WHITE, new Color(180, 180, 180), new Color(100, 100, 100)),
        BLUE(new Color(0, 100, 200, 128), new Color(20, 120, 220, 180), new Color(40, 140, 240, 200),
                Color.WHITE, new Color(200, 220, 255), new Color(120, 140, 160)),
        GREEN(new Color(0, 150, 50, 128), new Color(20, 170, 70, 180), new Color(40, 190, 90, 200),
                Color.WHITE, new Color(200, 255, 220), new Color(120, 160, 140)),
        RED(new Color(200, 50, 50, 128), new Color(220, 70, 70, 180), new Color(240, 90, 90, 200),
                Color.WHITE, new Color(255, 200, 200), new Color(160, 120, 120));

        public final Color base, hover, pressed, text, textHover, textDisabled;

        ColorTheme(Color base, Color hover, Color pressed, Color text, Color textHover, Color textDisabled) {
            this.base = base;
            this.hover = hover;
            this.pressed = pressed;
            this.text = text;
            this.textHover = textHover;
            this.textDisabled = textDisabled;
        }
    }

    public GuiCustomButton(String text, int buttonId, float xPosition, float yPosition, float radius, @NotNull FontRenderer fontRenderer) {
        this(text, buttonId, xPosition, yPosition, 200, 20, radius, fontRenderer, ColorTheme.DARK);
    }

    public GuiCustomButton(String text, int buttonId, float xPosition, float yPosition, float width, float height, float radius, @NotNull FontRenderer fontRenderer) {
        this(text, buttonId, xPosition, yPosition, width, height, radius, fontRenderer, ColorTheme.DARK);
    }

    public GuiCustomButton(String text, int buttonId, float xPosition, float yPosition) {
        this(text, buttonId, xPosition, yPosition, 200, 20, 8f, Fonts.interRegular.get(15), ColorTheme.DARK);
    }

    public GuiCustomButton(String text, int buttonId, int x, int y, int width, int height, int radius, @NotNull FontRenderer fontRenderer) {
        this(text, buttonId, x, y, width, height, radius, fontRenderer, ColorTheme.DARK);
    }

    public GuiCustomButton(String text, int buttonId, float xPosition, float yPosition, float width, float height,
                           float radius, @NotNull FontRenderer fontRenderer, @NotNull ColorTheme colorTheme) {
        super(buttonId, (int)xPosition, (int)yPosition, (int)width, (int)height, text);

        this.radius = radius;
        this.fontRenderer = fontRenderer;
        this.colorTheme = colorTheme;

        this.hoverAnimation = new SmoothStepAnimation(DEFAULT_ANIMATION_DURATION, 1);
        this.clickAnimation = new SmoothStepAnimation(CLICK_ANIMATION_DURATION, 1);
        this.disabledAnimation = new SmoothStepAnimation(200, 1);
    }

    public void drawButton(int mouseX, int mouseY) {
        drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!this.visible) return;

        boolean hovered = isHovered(mouseX, mouseY) && this.enabled;

        updateAnimations(hovered);

        double hoverProgress = hoverAnimation.getOutput();
        double clickProgress = clickAnimation.getOutput();
        double disabledProgress = disabledAnimation.getOutput();

        Color backgroundColor = calculateBackgroundColor(hoverProgress, clickProgress, disabledProgress);
        Color textColor = calculateTextColor(hovered, disabledProgress);

        float scale = 1.0f - ((float)clickProgress * CLICK_SCALE_REDUCTION);
        float scaledWidth = width * scale;
        float scaledHeight = height * scale;
        float scaledX = xPosition + (width - scaledWidth) / 2f;
        float scaledY = yPosition + (height - scaledHeight) / 2f;

        RoundedUtils.drawRound(scaledX, scaledY, scaledWidth, scaledHeight, radius, backgroundColor);

        if (hovered && hoverProgress > 0.1 && this.enabled) {
            drawHoverOutline(scaledX, scaledY, scaledWidth, scaledHeight, hoverProgress);
        }

        drawButtonText(scaledX, scaledY, scaledWidth, scaledHeight, textColor);

        if (!this.enabled && disabledProgress > 0.1) {
            drawDisabledOverlay(scaledX, scaledY, scaledWidth, scaledHeight, disabledProgress);
        }
    }

    private void updateAnimations(boolean hovered) {
        hoverAnimation.setDirection(hovered ? Direction.FORWARDS : Direction.BACKWARDS);

        disabledAnimation.setDirection(!this.enabled ? Direction.FORWARDS : Direction.BACKWARDS);

        if (isAnimating && System.currentTimeMillis() - lastClickTime > CLICK_ANIMATION_DURATION) {
            clickAnimation.setDirection(Direction.BACKWARDS);
            isAnimating = false;
        }
    }

    private Color calculateBackgroundColor(double hoverProgress, double clickProgress, double disabledProgress) {
        Color baseColor = colorTheme.base;
        Color hoverColor = colorTheme.hover;
        Color pressedColor = colorTheme.pressed;

        Color result = interpolateColor(baseColor, hoverColor, (float)hoverProgress);

        if (clickProgress > 0) {
            result = interpolateColor(result, pressedColor, (float)clickProgress);
        }

        if (disabledProgress > 0) {
            result = applyAlpha(result, 1.0f - (float)disabledProgress * DISABLED_ALPHA_MULTIPLIER);
        }

        return result;
    }

    private Color calculateTextColor(boolean hovered, double disabledProgress) {
        if (disabledProgress > 0.5) {
            return colorTheme.textDisabled;
        }
        return hovered ? colorTheme.textHover : colorTheme.text;
    }

    private void drawHoverOutline(float x, float y, float width, float height, double hoverProgress) {
        Color outlineColor = new Color(255, 255, 255, (int)(30 * hoverProgress));
        RoundedUtils.drawRound(x - 1, y - 1, width + 2, height + 2, radius, outlineColor);
    }

    private void drawButtonText(float x, float y, float width, float height, Color textColor) {
        float textX = x + width / 2f;
        float textY = y + fontRenderer.getMiddleOfBox(height) + 2;
        fontRenderer.drawCenteredString(displayString, textX, textY, textColor.getRGB());
    }

    private void drawDisabledOverlay(float x, float y, float width, float height, double disabledProgress) {
        Color overlayColor = new Color(0, 0, 0, (int)(50 * disabledProgress));
        RoundedUtils.drawRound(x, y, width, height, radius, overlayColor);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        boolean pressed = super.mousePressed(mc, mouseX, mouseY);

        if (pressed && this.enabled) {
            triggerClickAnimation();

            if (clickAction != null) {
                clickAction.run();
            }
        }

        return pressed;
    }

    private void triggerClickAnimation() {
        clickAnimation.setDirection(Direction.FORWARDS);
        lastClickTime = System.currentTimeMillis();
        isAnimating = true;
    }

    private boolean isHovered(int mouseX, int mouseY) {
        return MouseUtils.isHovered2(xPosition, yPosition, width, height, mouseX, mouseY);
    }

    private Color interpolateColor(Color color1, Color color2, float progress) {
        progress = Math.max(0, Math.min(1, progress));

        int r = (int)(color1.getRed() + (color2.getRed() - color1.getRed()) * progress);
        int g = (int)(color1.getGreen() + (color2.getGreen() - color1.getGreen()) * progress);
        int b = (int)(color1.getBlue() + (color2.getBlue() - color1.getBlue()) * progress);
        int a = (int)(color1.getAlpha() + (color2.getAlpha() - color1.getAlpha()) * progress);

        return new Color(r, g, b, a);
    }

    private Color applyAlpha(Color color, float alpha) {
        alpha = Math.max(0, Math.min(1, alpha));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(color.getAlpha() * alpha));
    }

    public void setClickAction(@Nullable Runnable clickAction) {
        this.clickAction = clickAction;
    }

    public void setColorTheme(@NotNull ColorTheme colorTheme) {
        this.colorTheme = colorTheme;
    }

    public void setRadius(float radius) {
        this.radius = Math.max(0, radius);
    }

    public void setFontRenderer(@NotNull FontRenderer fontRenderer) {
        this.fontRenderer = fontRenderer;
    }

    public ColorTheme getColorTheme() {
        return colorTheme;
    }

    public float getRadius() {
        return radius;
    }

    public FontRenderer getFontRenderer() {
        return fontRenderer;
    }

    @Nullable
    public Runnable getClickAction() {
        return clickAction;
    }

    public GuiCustomButton withClickAction(@NotNull Runnable action) {
        this.clickAction = action;
        return this;
    }

    public GuiCustomButton withTheme(@NotNull ColorTheme theme) {
        this.colorTheme = theme;
        return this;
    }

    public GuiCustomButton withRadius(float radius) {
        this.radius = radius;
        return this;
    }
}