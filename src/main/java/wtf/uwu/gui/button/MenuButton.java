package wtf.uwu.gui.button;

import wtf.uwu.gui.font.Fonts;
import wtf.uwu.utils.animations.Animation;
import wtf.uwu.utils.animations.Direction;
import wtf.uwu.utils.animations.impl.SmoothStepAnimation;
import wtf.uwu.utils.render.MouseUtils;
import wtf.uwu.utils.render.RoundedUtils;

import java.awt.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MenuButton implements Button {

    public final String text;
    public Animation hoverAnimation;
    public Animation clickAnimation;

    public float x, y, width, height;
    public Runnable clickAction;
    public boolean isEnabled = true;

    public static final Color DEFAULT_COLOR = new Color(0, 0, 0, 128);
    public static final Color HOVER_COLOR = new Color(40, 40, 40, 180);
    public static final Color DISABLED_COLOR = new Color(0, 0, 0, 64);
    public static final Color TEXT_COLOR = Color.WHITE;
    public static final Color TEXT_DISABLED_COLOR = new Color(128, 128, 128);

    public static final float BORDER_RADIUS = 8f;
    public static final int FONT_SIZE = 15;
    public static final int ANIMATION_DURATION = 400;

    private static final int DEFAULT_COLOR_RGB = DEFAULT_COLOR.getRGB();
    private static final int HOVER_COLOR_RGB = HOVER_COLOR.getRGB();
    private static final int DISABLED_COLOR_RGB = DISABLED_COLOR.getRGB();
    private static final int TEXT_COLOR_RGB = TEXT_COLOR.getRGB();
    private static final int TEXT_DISABLED_COLOR_RGB = TEXT_DISABLED_COLOR.getRGB();

    private static final ScheduledExecutorService ANIMATION_EXECUTOR = Executors.newScheduledThreadPool(2);

    private float cachedTextY = 0;
    private boolean textYCached = false;
    private float lastHeight = 0;

    private final ColorCache colorCache = new ColorCache();

    public MenuButton(String text) {
        this.text = text;
    }

    public MenuButton(String text, Runnable clickAction) {
        this.text = text;
        this.clickAction = clickAction;
    }

    @Override
    public void initGui() {
        hoverAnimation = new SmoothStepAnimation(ANIMATION_DURATION, 1);
        clickAnimation = new SmoothStepAnimation(150, 1);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        boolean hovered = isHovered(mouseX, mouseY) && isEnabled;

        hoverAnimation.setDirection(hovered ? Direction.FORWARDS : Direction.BACKWARDS);

        double hoverProgress = hoverAnimation.getOutput();
        double clickProgress = clickAnimation.getOutput();

        int backgroundColor = getBackgroundColor(hoverProgress);

        float scale = 1.0f - ((float)clickProgress * 0.05f);
        float scaledWidth = width * scale;
        float scaledHeight = height * scale;
        float scaledX = x + (width - scaledWidth) * 0.5f;
        float scaledY = y + (height - scaledHeight) * 0.5f;

        RoundedUtils.drawRound(scaledX, scaledY, scaledWidth, scaledHeight, BORDER_RADIUS, new Color(backgroundColor, true));

        if (hovered && hoverProgress > 0.1) {
            drawHoverOutline(scaledX, scaledY, scaledWidth, scaledHeight, hoverProgress, backgroundColor);
        }

        drawText(scaledX, scaledY, scaledWidth, scaledHeight);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (!isEnabled) return;

        if (isHovered(mouseX, mouseY) && button == 0) {
            clickAnimation.setDirection(Direction.FORWARDS);

            ANIMATION_EXECUTOR.schedule(() -> {
                clickAnimation.setDirection(Direction.BACKWARDS);
            }, 75, TimeUnit.MILLISECONDS);

            if (clickAction != null) {
                clickAction.run();
            }
        }
    }

    private boolean isHovered(int mouseX, int mouseY) {
        return MouseUtils.isHovered2(x, y, width, height, mouseX, mouseY);
    }

    private int getBackgroundColor(double hoverProgress) {
        if (!isEnabled) {
            return DISABLED_COLOR_RGB;
        }

        if (hoverProgress <= 0.0) {
            return DEFAULT_COLOR_RGB;
        }
        if (hoverProgress >= 1.0) {
            return HOVER_COLOR_RGB;
        }

        return colorCache.getInterpolatedColor(DEFAULT_COLOR, HOVER_COLOR, (float)hoverProgress);
    }

    private void drawHoverOutline(float scaledX, float scaledY, float scaledWidth, float scaledHeight,
                                  double hoverProgress, int backgroundColor) {
        int outlineAlpha = (int)(50 * hoverProgress);
        int outlineColor = (outlineAlpha << 24) | 0xFFFFFF;

        RoundedUtils.drawRound(scaledX - 1, scaledY - 1, scaledWidth + 2, scaledHeight + 2,
                BORDER_RADIUS, new Color(outlineColor, true));
        RoundedUtils.drawRound(scaledX, scaledY, scaledWidth, scaledHeight,
                BORDER_RADIUS, new Color(backgroundColor, true));
    }

    private void drawText(float scaledX, float scaledY, float scaledWidth, float scaledHeight) {
        if (!textYCached || lastHeight != scaledHeight) {
            cachedTextY = scaledY + Fonts.interRegular.get(FONT_SIZE).getMiddleOfBox(scaledHeight) + 2;
            textYCached = true;
            lastHeight = scaledHeight;
        }

        int textColor = isEnabled ? TEXT_COLOR_RGB : TEXT_DISABLED_COLOR_RGB;
        float textX = scaledX + scaledWidth * 0.5f;

        Fonts.interRegular.get(FONT_SIZE).drawCenteredString(text, textX, cachedTextY, textColor);
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        invalidateTextCache();
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        invalidateTextCache();
    }

    public void setBounds(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        invalidateTextCache();
    }

    public void setClickAction(Runnable clickAction) {
        this.clickAction = clickAction;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getText() {
        return text;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }

    private void invalidateTextCache() {
        textYCached = false;
    }

    public void cleanup() {
    }

    public static void shutdownAnimationExecutor() {
        ANIMATION_EXECUTOR.shutdown();
        try {
            if (!ANIMATION_EXECUTOR.awaitTermination(1, TimeUnit.SECONDS)) {
                ANIMATION_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            ANIMATION_EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static class ColorCache {
        private Color lastColor1, lastColor2;
        private float lastProgress;
        private int cachedResult;

        public int getInterpolatedColor(Color color1, Color color2, float progress) {
            if (color1.equals(lastColor1) && color2.equals(lastColor2) &&
                    Math.abs(progress - lastProgress) < 0.001f) {
                return cachedResult;
            }

            progress = Math.max(0, Math.min(1, progress));

            int r = (int)(color1.getRed() + (color2.getRed() - color1.getRed()) * progress);
            int g = (int)(color1.getGreen() + (color2.getGreen() - color1.getGreen()) * progress);
            int b = (int)(color1.getBlue() + (color2.getBlue() - color1.getBlue()) * progress);
            int a = (int)(color1.getAlpha() + (color2.getAlpha() - color1.getAlpha()) * progress);

            lastColor1 = color1;
            lastColor2 = color2;
            lastProgress = progress;
            cachedResult = (a << 24) | (r << 16) | (g << 8) | b;

            return cachedResult;
        }
    }
}