package wtf.uwu.gui.altmanager.group;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import wtf.uwu.gui.font.FontRenderer;
import wtf.uwu.utils.render.RenderUtils;
import wtf.uwu.utils.render.AnimatedBackgroundUtils;

import java.awt.*;

@Getter
public class GuiRoundedGroup extends AbstractGroup {
    protected final int radius;
    private static boolean useAnimatedBackground = true; // Toggle for animated background

    public GuiRoundedGroup(String title, int xPosition, int yPosition, int width, int height, int radius,
                           FontRenderer titleFontRenderer) {
        super(title, xPosition, yPosition, width, height, titleFontRenderer);
        this.radius = radius;
    }

    public GuiRoundedGroup(String title, int xPosition, int yPosition, int width, int height, int radius) {
        super(title, xPosition, yPosition, width, height);
        this.radius = radius;
    }

    @Override
    public void drawGroup(Minecraft mc, int mouseX, int mouseY) {
        if (this.hidden) return;

        // Draw animated background if enabled
        if (useAnimatedBackground) {
            // Get the screen dimensions for full background
            int screenWidth = mc.displayWidth / mc.gameSettings.guiScale;
            int screenHeight = mc.displayHeight / mc.gameSettings.guiScale;
            AnimatedBackgroundUtils.drawMinimalAnimatedBackground(screenWidth, screenHeight);
        }

        // Draw the group panel with slightly more transparency to blend with animated background
        RenderUtils.drawRoundedRect(this.xPosition,
                this.yPosition, this.width, this.height, this.radius,
                new Color(0, 0, 0, useAnimatedBackground ? 120 : 80).getRGB());

        if (this.title != null) {
            // Enhanced title with better visibility over animated background
            Color titleColor = useAnimatedBackground ?
                    new Color(220, 220, 220) : new Color(198, 198, 198);

            this.titleFontRenderer.drawString(this.title,
                    this.xPosition + (this.width - this.titleFontRenderer.getStringWidth(this.title)) / 2.0F,
                    this.yPosition + 4,
                    titleColor.getRGB());
        }
    }

    /**
     * Toggle animated background on/off
     */
    public static void setAnimatedBackground(boolean enabled) {
        useAnimatedBackground = enabled;
    }

    /**
     * Check if animated background is enabled
     */
    public static boolean isAnimatedBackgroundEnabled() {
        return useAnimatedBackground;
    }
}