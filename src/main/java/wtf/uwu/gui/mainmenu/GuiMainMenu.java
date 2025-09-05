package wtf.uwu.gui.mainmenu;

import lombok.Getter;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.util.EnumChatFormatting;
import wtf.uwu.UwU;
import wtf.uwu.gui.button.MenuButton;
import wtf.uwu.gui.font.Fonts;
import wtf.uwu.utils.render.RoundedUtils;
import wtf.uwu.utils.render.shader.impl.Blur;
import wtf.uwu.utils.render.shader.impl.MainMenu;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiMainMenu extends GuiScreen {

    private static final float BUTTON_WIDTH = 140f;
    private static final float BUTTON_HEIGHT = 25f;
    private static final float BUTTON_VERTICAL_SPACING = 3f;
    private static final float PANEL_HORIZONTAL_PADDING = 20f;
    private static final float PANEL_TOP_OFFSET_FROM_CENTER = 60f;
    private static final float PANEL_CONTENT_HEIGHT = 200f;
    private static final float PANEL_CORNER_RADIUS = 10f;
    private static final Color PANEL_BACKGROUND_COLOR = new Color(0, 0, 0, 64);
    private static final int BLUR_EFFECT_RADIUS = 10;
    private static final int BLUR_EFFECT_PASSES = 3;
    private static final float TITLE_FONT_SIZE = 35f;
    private static final float WELCOME_MSG_FONT_SIZE = 14f;
    private static final float CREDITS_FONT_SIZE = 12f;
    private static final float CHANGELOG_TITLE_FONT_SIZE = 20f;
    private static final float CHANGELOG_ENTRY_FONT_SIZE = 15f;
    private static final int CHANGELOG_MARGIN_LEFT = 5;
    private static final int CHANGELOG_MARGIN_TOP = 3;
    private static final int CHANGELOG_SPACING_AFTER_TITLE = 2;
    private static final int CHANGELOG_SPACING_BETWEEN_ENTRIES = 2;
    private static final float BG_BUTTON_WIDTH = 130f;
    private static final float BG_BUTTON_HEIGHT = 20f;
    private static BackgroundStyle currentBackgroundStyle = BackgroundStyle.CYBERPUNK;
    private static boolean useCustomSpeed = false;
    private static float customSpeed = 1.0f;
    private static final List<String> DEVELOPERS = List.of(
            "Main Developer: Wesk",
            "Events Designer: Wesk",
            "Special Thanks: Wesk"
    );

    private final List<MenuButton> menuOptions = new ArrayList<>();
    private final List<ChangeLogEntry> changeLogEntries = new ArrayList<>();
    private MenuButton backgroundButton;

    public GuiMainMenu() {
        menuOptions.add(new MenuButton("Singleplayer"));
        menuOptions.add(new MenuButton("Multiplayer"));
        menuOptions.add(new MenuButton("Alt Manager"));
        menuOptions.add(new MenuButton("Settings"));
        menuOptions.add(new MenuButton("Exit Game"));
        changeLogEntries.add(new ChangeLogEntry("Welcome to UwU Client!", ChangeLogEntryType.ADDITION));
        changeLogEntries.add(new ChangeLogEntry("suck", ChangeLogEntryType.IMPROVEMENT));
        changeLogEntries.add(new ChangeLogEntry("my", ChangeLogEntryType.ADDITION));
        changeLogEntries.add(new ChangeLogEntry("dick", ChangeLogEntryType.ADDITION));
        changeLogEntries.add(new ChangeLogEntry("", ChangeLogEntryType.IMPROVEMENT));
        changeLogEntries.add(new ChangeLogEntry("", ChangeLogEntryType.FIX));
        changeLogEntries.add(new ChangeLogEntry("", ChangeLogEntryType.OTHER));
    }


    private int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }
    private int clampAlpha(float alpha) {
        return Math.max(0, Math.min(255, (int)(alpha * 255)));
    }

    @Override
    public void initGui() {
        for (MenuButton button : menuOptions) {
            try {
                button.initGui();
            } catch (Exception e) {
                System.err.println("Error initializing menu button: " + button.text);
                e.printStackTrace();
            }
        }

        updateBackgroundButton();
    }

    private void updateBackgroundButton() {
        try {
            backgroundButton = new MenuButton("BG: " + currentBackgroundStyle.getDisplayName());
            backgroundButton.x = this.width - BG_BUTTON_WIDTH - 10;
            backgroundButton.y = 10;
            backgroundButton.width = BG_BUTTON_WIDTH;
            backgroundButton.height = BG_BUTTON_HEIGHT;
            backgroundButton.initGui();
            backgroundButton.clickAction = this::cycleBackgroundStyle;

        } catch (Exception e) {
            System.err.println("Error creating background button");
            e.printStackTrace();
            backgroundButton = null;
        }
    }

    private void cycleBackgroundStyle() {
        try {
            BackgroundStyle[] styles = BackgroundStyle.values();
            int currentIndex = 0;
            for (int i = 0; i < styles.length; i++) {
                if (styles[i] == currentBackgroundStyle) {
                    currentIndex = i;
                    break;
                }
            }
            currentBackgroundStyle = styles[(currentIndex + 1) % styles.length];
            updateBackgroundButton();
        } catch (Exception e) {
            System.err.println("Error cycling background style");
            e.printStackTrace();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        try {
            drawBackgroundShader();
            float panelActualWidth = BUTTON_WIDTH + PANEL_HORIZONTAL_PADDING * 2;
            float panelX = (this.width / 2f) - panelActualWidth / 2f;
            float panelY = (this.height / 2f) - PANEL_TOP_OFFSET_FROM_CENTER;
            RoundedUtils.drawRound(panelX, panelY, panelActualWidth, PANEL_CONTENT_HEIGHT, PANEL_CORNER_RADIUS, PANEL_BACKGROUND_COLOR);
            Blur.startBlur();
            RoundedUtils.drawRound(panelX, panelY, panelActualWidth, PANEL_CONTENT_HEIGHT, PANEL_CORNER_RADIUS, PANEL_BACKGROUND_COLOR);
            Blur.endBlur(BLUR_EFFECT_RADIUS, BLUR_EFFECT_PASSES);
            float contentVerticalOffsetInPanel = 20f;
            float titleFontHeight = Fonts.interBold.get(TITLE_FONT_SIZE).getHeight();
            float baseContentY = panelY + contentVerticalOffsetInPanel;

            Fonts.interBold.get(TITLE_FONT_SIZE).drawCenteredString(
                    UwU.INSTANCE.getClientName(),
                    panelX + panelActualWidth / 2f,
                    baseContentY,
                    -1);

            float currentButtonY = baseContentY + titleFontHeight + 5;

            for (MenuButton option : menuOptions) {
                try {
                    option.x = panelX + PANEL_HORIZONTAL_PADDING;
                    option.y = currentButtonY;
                    option.width = BUTTON_WIDTH;
                    option.height = BUTTON_HEIGHT;
                    option.clickAction = () -> {
                        try {
                            switch (option.text) {
                                case "Singleplayer":
                                    mc.displayGuiScreen(new GuiSelectWorld(this));
                                    break;
                                case "Multiplayer":
                                    mc.displayGuiScreen(new GuiMultiplayer(this));
                                    break;
                                case "Alt Manager":
                                    mc.displayGuiScreen(UwU.INSTANCE.getAltRepositoryGUI());
                                    break;
                                case "Settings":
                                    mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
                                    break;
                                case "Exit Game":
                                    mc.shutdown();
                                    break;
                            }
                        } catch (Exception e) {
                            System.err.println("Error executing button action: " + option.text);
                            e.printStackTrace();
                        }
                    };

                    option.drawScreen(mouseX, mouseY);
                    currentButtonY += BUTTON_HEIGHT + BUTTON_VERTICAL_SPACING;

                } catch (Exception e) {
                    System.err.println("Error drawing menu button: " + option.text);
                    e.printStackTrace();
                }
            }
            if (backgroundButton != null) {
                try {
                    backgroundButton.drawScreen(mouseX, mouseY);
                } catch (Exception e) {
                    System.err.println("Error drawing background button");
                    e.printStackTrace();
                }
            }

            drawChangelogs();
            drawWelcomeMessage();
            drawCreditsSection();

            super.drawScreen(mouseX, mouseY, partialTicks);

        } catch (Exception e) {
            System.err.println("Error in drawScreen method");
            e.printStackTrace();
        }
    }

    private void drawChangelogs() {
        try {
            float currentLogY = CHANGELOG_MARGIN_TOP;
            Fonts.interBold.get(CHANGELOG_TITLE_FONT_SIZE).drawStringWithShadow(
                    "Recent Updates",
                    CHANGELOG_MARGIN_LEFT,
                    currentLogY,
                    -1);
            currentLogY += Fonts.interBold.get(CHANGELOG_TITLE_FONT_SIZE).getHeight() + CHANGELOG_SPACING_AFTER_TITLE;

            for (ChangeLogEntry entry : changeLogEntries) {
                if (entry != null && entry.getDetails() != null) {
                    Fonts.interBold.get(CHANGELOG_ENTRY_FONT_SIZE).drawStringWithShadow(
                            entry.type.getPrefix() + " " + entry.getDetails(),
                            CHANGELOG_MARGIN_LEFT,
                            currentLogY,
                            entry.type.getTextColor());
                    currentLogY += Fonts.interBold.get(CHANGELOG_ENTRY_FONT_SIZE).getHeight() + CHANGELOG_SPACING_BETWEEN_ENTRIES;
                }
            }
        } catch (Exception e) {
            System.err.println("Error drawing changelogs");
            e.printStackTrace();
        }
    }

    private void drawWelcomeMessage() {
        try {
            String welcomeText = "Welcome back, " + EnumChatFormatting.AQUA + UwU.INSTANCE.getDiscordRP().getName();
            float welcomeTextWidth = Fonts.interMedium.get(WELCOME_MSG_FONT_SIZE).getStringWidth(welcomeText);
            float welcomeTextHeight = Fonts.interMedium.get(WELCOME_MSG_FONT_SIZE).getHeight();
            Fonts.interMedium.get(WELCOME_MSG_FONT_SIZE).drawStringWithShadow(
                    welcomeText,
                    this.width - (2 + welcomeTextWidth),
                    this.height - (2 + welcomeTextHeight),
                    -1);
        } catch (Exception e) {
            System.err.println("Error drawing welcome message");
            e.printStackTrace();
        }
    }

    private void drawCreditsSection() {
        try {
            float creditsX = 5;
            float creditsTitleHeight = Fonts.interBold.get(CREDITS_FONT_SIZE + 2).getHeight();
            float creditsLineHeight = Fonts.interMedium.get(CREDITS_FONT_SIZE).getHeight();
            float versionLineHeight = Fonts.interMedium.get(CREDITS_FONT_SIZE - 1).getHeight();
            float totalCreditsHeight = creditsTitleHeight + 3 + (DEVELOPERS.size() * (creditsLineHeight + 1)) + 5 + versionLineHeight;
            float currentCreditsY = this.height - totalCreditsHeight - 5;
            Fonts.interBold.get(CREDITS_FONT_SIZE + 2).drawStringWithShadow(
                    EnumChatFormatting.GOLD + "Credits",
                    creditsX,
                    currentCreditsY,
                    -1);
            currentCreditsY += creditsTitleHeight + 3;

            for (String developer : DEVELOPERS) {
                Fonts.interMedium.get(CREDITS_FONT_SIZE).drawStringWithShadow(
                        EnumChatFormatting.GRAY + developer,
                        creditsX,
                        currentCreditsY,
                        -1);
                currentCreditsY += creditsLineHeight + 1;
            }

            currentCreditsY += 5;
            String versionText = EnumChatFormatting.DARK_GRAY + "Pre release 4";
            Fonts.interMedium.get(CREDITS_FONT_SIZE - 1).drawStringWithShadow(
                    versionText,
                    creditsX,
                    currentCreditsY,
                    -1);
        } catch (Exception e) {
            System.err.println("Error drawing credits section");
            e.printStackTrace();
        }
    }

    private void drawBackgroundShader() {
        try {
            long startTime = UwU.INSTANCE.getStartTimeLong();

            switch (currentBackgroundStyle) {
                case DISABLED:
                    drawDisabledBackground();
                    break;
                case CYBERPUNK:
                    drawCyberpunkBackground(startTime);
                    break;
                case GALAXY:
                    drawGalaxyBackground(startTime);
                    break;
                case NEON_CITY:
                    drawNeonCityBackground(startTime);
                    break;
                case AURORA:
                    drawAuroraBackground(startTime);
                    break;
                case MATRIX:
                    drawMatrixBackground(startTime);
                    break;
                case RETRO_WAVE:
                    drawRetroWaveBackground(startTime);
                    break;
                case PLASMA:
                    drawPlasmaBackground(startTime);
                    break;
                case FIRE:
                    drawFireBackground(startTime);
                    break;
                case OCEAN:
                    drawOceanBackground(startTime);
                    break;
                case RAINBOW:
                    drawRainbowBackground(startTime);
                    break;
                case DIGITAL:
                    drawDigitalBackground(startTime);
                    break;
                case SPACE_WARP:
                    drawSpaceWarpBackground(startTime);
                    break;
                case ELECTRIC:
                    drawElectricBackground(startTime);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error drawing background shader");
            e.printStackTrace();
            drawDisabledBackground();
        }
    }


    private void drawDisabledBackground() {
        try {
            drawGradientRect(0, 0, this.width, this.height,
                    new Color(0, 0, 0, 255).getRGB(),
                    new Color(20, 20, 20, 255).getRGB());
        } catch (Exception e) {
            System.err.println("Error drawing disabled background");
            e.printStackTrace();
        }
    }


    private void drawCyberpunkBackground(long startTime) {
        try {
            float time = (startTime % 15000) / 1000.0f;
            int topColor = new Color(5, 5, 30, 255).getRGB();
            int bottomColor = new Color(30, 0, 50, 255).getRGB();
            drawGradientRect(0, 0, this.width, this.height, topColor, bottomColor);
            drawElectricGrid(time);
            drawNeonParticles(time, new Color(0, 255, 255), new Color(255, 0, 255));
            drawScanLines(time);
        } catch (Exception e) {
            System.err.println("Error drawing cyberpunk background");
            e.printStackTrace();
        }
    }

    private void drawGalaxyBackground(long startTime) {
        try {
            float time = (startTime % 25000) / 1000.0f;
            int topColor = new Color(5, 0, 20, 255).getRGB();
            int bottomColor = new Color(0, 10, 40, 255).getRGB();
            drawGradientRect(0, 0, this.width, this.height, topColor, bottomColor);
            drawStarField(time);
            drawNebulaClouds(time);
            drawTwinklingStars(time);
        } catch (Exception e) {
            System.err.println("Error drawing galaxy background");
            e.printStackTrace();
        }
    }

    private void drawNeonCityBackground(long startTime) {
        try {
            float time = (startTime % 20000) / 1000.0f;
            int topColor = new Color(10, 0, 30, 255).getRGB();
            int bottomColor = new Color(50, 20, 80, 255).getRGB();
            drawGradientRect(0, 0, this.width, this.height, topColor, bottomColor);
            drawNeonStreaks(time);
            drawCityGlow(time);
            drawDigitalRain(time);
        } catch (Exception e) {
            System.err.println("Error drawing neon city background");
            e.printStackTrace();
        }
    }

    private void drawAuroraBackground(long startTime) {
        try {
            float time = (startTime % 30000) / 1000.0f;
            int topColor = new Color(0, 5, 15, 255).getRGB();
            int bottomColor = new Color(10, 20, 40, 255).getRGB();
            drawGradientRect(0, 0, this.width, this.height, topColor, bottomColor);
            drawAuroraWaves(time);
            drawShimmeringParticles(time);
        } catch (Exception e) {
            System.err.println("Error drawing aurora background");
            e.printStackTrace();
        }
    }


    private void drawMatrixBackground(long startTime) {
        try {
            float time = (startTime % 12000) / 1000.0f;
            int topColor = new Color(0, 5, 0, 255).getRGB();
            int bottomColor = new Color(0, 20, 0, 255).getRGB();
            drawGradientRect(0, 0, this.width, this.height, topColor, bottomColor);
            drawMatrixRain(time);
            drawMatrixGlow(time);
        } catch (Exception e) {
            System.err.println("Error drawing matrix background");
            e.printStackTrace();
        }
    }

    private void drawRetroWaveBackground(long startTime) {
        try {
            float time = (startTime % 18000) / 1000.0f;
            int topColor = new Color(50, 0, 80, 255).getRGB();
            int bottomColor = new Color(100, 20, 120, 255).getRGB();
            drawGradientRect(0, 0, this.width, this.height, topColor, bottomColor);
            drawRetroGrid(time);
            drawRetroSun(time);
            drawSynthWaves(time);
        } catch (Exception e) {
            System.err.println("Error drawing retro wave background");
            e.printStackTrace();
        }
    }

    private void drawPlasmaBackground(long startTime) {
        try {
            float time = (startTime % 10000) / 1000.0f;

            // Dynamic plasma colors
            for (int x = 0; x < this.width; x += 4) {
                for (int y = 0; y < this.height; y += 4) {
                    float plasma = (float) (Math.sin(x * 0.01f + time) +
                            Math.sin(y * 0.01f + time * 1.2f) +
                            Math.sin((x + y) * 0.01f + time * 0.8f));

                    int r = clampColor((int) (128 + 127 * Math.sin(plasma + time)));
                    int g = clampColor((int) (128 + 127 * Math.sin(plasma + time + 2)));
                    int b = clampColor((int) (128 + 127 * Math.sin(plasma + time + 4)));

                    Color plasmaColor = new Color(r, g, b, 150);
                    RoundedUtils.drawRound(x, y, 4, 4, 0, plasmaColor);
                }
            }
        } catch (Exception e) {
            System.err.println("Error drawing plasma background");
            e.printStackTrace();
        }
    }

    private void drawFireBackground(long startTime) {
        try {
            float time = (startTime % 8000) / 1000.0f;

            int topColor = new Color(60, 0, 0, 255).getRGB();
            int bottomColor = new Color(120, 30, 0, 255).getRGB();
            drawGradientRect(0, 0, this.width, this.height, topColor, bottomColor);

            for (int i = 0; i < 50; i++) {
                float x = (float) (this.width * 0.1f + (this.width * 0.8f) * ((i * 73.2f) % 1.0f));
                float baseY = this.height - (i % 5) * 20;
                float y = (float) (baseY - (time * 100 + i * 15) % (this.height + 50));

                float intensity = (float) Math.max(0.3f, 1.0f - (this.height - y) / this.height);
                int red = clampColor((int) (255 * intensity));
                int green = clampColor((int) (100 * intensity));
                int blue = 0;

                Color fireColor = new Color(red, green, blue, clampAlpha(intensity * 0.7f));
                float size = (float) Math.max(2, 8 * intensity);
                RoundedUtils.drawRound(x - size/2, y - size/2, size, size, size/2, fireColor);
            }

            for (int wave = 0; wave < 4; wave++) {
                for (int x = 0; x < this.width; x += 3) {
                    float waveHeight = (float) (Math.sin((x * 0.01f) + time * 2f + wave) * 20);
                    float y = this.height - 50 - wave * 15 + waveHeight;

                    Color waveColor = new Color(255 - wave * 40, 100 - wave * 20, 0, 60 - wave * 10);
                    RoundedUtils.drawRound(x, y, 3, 8, 1, waveColor);
                }
            }
        } catch (Exception e) {
            System.err.println("Error drawing fire background");
            e.printStackTrace();
        }
    }

    private void drawOceanBackground(long startTime) {
        try {
            float time = (startTime % 20000) / 1000.0f;

            int topColor = new Color(0, 20, 40, 255).getRGB();
            int bottomColor = new Color(0, 50, 100, 255).getRGB();
            drawGradientRect(0, 0, this.width, this.height, topColor, bottomColor);

            for (int wave = 0; wave < 6; wave++) {
                Color waveColor = new Color(50 + wave * 20, 100 + wave * 25, 200 + wave * 10, 40);

                for (int x = 0; x < this.width; x += 2) {
                    float waveHeight = (float) (Math.sin((x * 0.008f) + time * 0.5f + wave * 0.5f) * 25);
                    float y = this.height * 0.6f + wave * 15 + waveHeight;

                    RoundedUtils.drawRound(x, y, 2, 6, 1, waveColor);
                }
            }

            for (int i = 0; i < 30; i++) {
                float x = (float) ((i * 127.3f) % this.width);
                float y = (float) ((this.height + 100) - (time * 30 + i * 20) % (this.height + 150));

                float alpha = (float) Math.max(0.2f, 0.6f + 0.4f * Math.sin(time * 3f + i));
                Color bubbleColor = new Color(150, 200, 255, clampAlpha(alpha));

                float size = (float) Math.max(1, 3 + 2 * Math.sin(time * 2f + i));
                RoundedUtils.drawRound(x - size/2, y - size/2, size, size, size/2, bubbleColor);
            }
        } catch (Exception e) {
            System.err.println("Error drawing ocean background");
            e.printStackTrace();
        }
    }

    private void drawRainbowBackground(long startTime) {
        try {
            float time = (startTime % 6000) / 1000.0f;

            for (int y = 0; y < this.height; y += 3) {
                float hue = (float) ((y * 0.01f + time * 0.5f) % 1.0f);
                Color rainbowColor = Color.getHSBColor(hue, 0.8f, 0.6f);
                Color translucentColor = new Color(rainbowColor.getRed(), rainbowColor.getGreen(), rainbowColor.getBlue(), 100);
                RoundedUtils.drawRound(0, y, this.width, 3, 0, translucentColor);
            }

            for (int i = 0; i < 25; i++) {
                float x = (float) (this.width * 0.1f + (this.width * 0.8f) * Math.sin(time * 0.8f + i * 0.3f));
                float y = (float) (this.height * 0.1f + (this.height * 0.8f) * Math.cos(time * 0.6f + i * 0.4f));

                float hue = (float) ((time + i * 0.1f) % 1.0f);
                Color particleColor = Color.getHSBColor(hue, 1.0f, 1.0f);
                Color translucentParticle = new Color(particleColor.getRed(), particleColor.getGreen(), particleColor.getBlue(), 180);

                float size = (float) Math.max(3, 6 + 3 * Math.sin(time * 4f + i));
                RoundedUtils.drawRound(x - size/2, y - size/2, size, size, size/2, translucentParticle);
            }
        } catch (Exception e) {
            System.err.println("Error drawing rainbow background");
            e.printStackTrace();
        }
    }

    private void drawDigitalBackground(long startTime) {
        try {
            float time = (startTime % 12000) / 1000.0f;

            int topColor = new Color(0, 10, 20, 255).getRGB();
            int bottomColor = new Color(0, 30, 60, 255).getRGB();
            drawGradientRect(0, 0, this.width, this.height, topColor, bottomColor);

            Color circuitColor = new Color(0, 255, 255, 80);
            float spacing = 80f;

            for (float x = 0; x < this.width; x += spacing) {
                float pulse = (float) Math.max(0.3f, 0.8f + 0.5f * Math.sin(time * 2f + x * 0.01f));
                Color lineColor = new Color(0, 255, 255, clampAlpha(pulse * 0.4f));
                RoundedUtils.drawRound(x, 0, 2, this.height, 0, lineColor);

                for (float y = spacing/2; y < this.height; y += spacing) {
                    float nodeSize = (float) Math.max(4, 8 + 4 * Math.sin(time * 3f + x * 0.01f + y * 0.01f));
                    Color nodeColor = new Color(0, 255, 255, clampAlpha(pulse * 0.8f));
                    RoundedUtils.drawRound(x - nodeSize/2, y - nodeSize/2, nodeSize, nodeSize, nodeSize/2, nodeColor);
                }
            }

            for (int i = 0; i < 15; i++) {
                float x = (float) ((time * 150 + i * 80) % (this.width + 100));
                float y = spacing/2 + (i % 6) * spacing;

                Color packetColor = new Color(255, 255, 0, 150);
                RoundedUtils.drawRound(x, y - 2, 6, 4, 2, packetColor);
            }
        } catch (Exception e) {
            System.err.println("Error drawing digital background");
            e.printStackTrace();
        }
    }

    private void drawSpaceWarpBackground(long startTime) {
        try {
            float time = (startTime % 5000) / 1000.0f;


            int topColor = new Color(0, 0, 15, 255).getRGB();
            int bottomColor = new Color(5, 0, 25, 255).getRGB();
            drawGradientRect(0, 0, this.width, this.height, topColor, bottomColor);

            float centerX = this.width / 2f;
            float centerY = this.height / 2f;


            for (int i = 0; i < 30; i++) {
                float angle = (float) (i * Math.PI * 2 / 30);
                float distance = (float) ((time * 200 + i * 50) % 400);

                float startX = (float) (centerX + Math.cos(angle) * 50);
                float startY = (float) (centerY + Math.sin(angle) * 50);
                float endX = (float) (centerX + Math.cos(angle) * (50 + distance));
                float endY = (float) (centerY + Math.sin(angle) * (50 + distance));

                float alpha = Math.max(0.1f, 1.0f - distance / 400f);
                Color warpColor = new Color(100, 150, 255, clampAlpha(alpha));


                int segments = 10;
                for (int s = 0; s < segments; s++) {
                    float t = (float) s / segments;
                    float x = startX + (endX - startX) * t;
                    float y = startY + (endY - startY) * t;
                    RoundedUtils.drawRound(x - 1, y - 1, 2, 2, 1, warpColor);
                }
            }

            for (int i = 0; i < 5; i++) {
                float radius = (float) (20 + i * 15 + 10 * Math.sin(time * 4f));
                float alpha = 0.5f - i * 0.08f;
                Color coreColor = new Color(255, 255, 255, clampAlpha(alpha));
                RoundedUtils.drawRound(centerX - radius, centerY - radius, radius * 2, radius * 2, radius, coreColor);
            }
        } catch (Exception e) {
            System.err.println("Error drawing space warp background");
            e.printStackTrace();
        }
    }

    private void drawElectricBackground(long startTime) {
        try {
            float time = (startTime % 8000) / 1000.0f;

            int topColor = new Color(10, 0, 30, 255).getRGB();
            int bottomColor = new Color(30, 10, 60, 255).getRGB();
            drawGradientRect(0, 0, this.width, this.height, topColor, bottomColor);

            for (int i = 0; i < 8; i++) {
                if ((time * 10 + i) % 3 < 0.1f) { // Flash effect
                    float startX = (float) ((i * 137.5f) % this.width);
                    float startY = 0;
                    float currentX = startX;
                    float currentY = startY;

                    Color lightningColor = new Color(255, 255, 0, 200);

                    for (int segment = 0; segment < 15; segment++) {
                        float nextX = (float) (currentX + (Math.sin(time * 20 + segment) * 20));
                        float nextY = currentY + this.height / 15f;

                        int steps = 5;
                        for (int step = 0; step < steps; step++) {
                            float t = (float) step / steps;
                            float x = currentX + (nextX - currentX) * t;
                            float y = currentY + (nextY - currentY) * t;
                            RoundedUtils.drawRound(x - 1, y, 3, 3, 1, lightningColor);
                        }

                        currentX = nextX;
                        currentY = nextY;

                        if (currentY > this.height) break;
                    }
                }
            }

            for (int i = 0; i < 40; i++) {
                float x = (float) ((i * 91.7f) % this.width);
                float y = (float) ((i * 127.3f) % this.height);

                float spark = (float) Math.max(0.2f, 0.8f + 0.6f * Math.sin(time * 8f + i * 0.5f));
                Color sparkColor = new Color(255, 255, 100, clampAlpha(spark * 0.6f));

                float size = (float) Math.max(1, 3 + 2 * Math.sin(time * 6f + i));
                RoundedUtils.drawRound(x - size/2, y - size/2, size, size, size/2, sparkColor);
            }

            for (int x = 0; x < this.width; x += 40) {
                for (int y = 0; y < this.height; y += 40) {
                    float fieldIntensity = (float) Math.max(0.1f, 0.3f + 0.2f * Math.sin(time * 3f + x * 0.01f + y * 0.01f));
                    Color fieldColor = new Color(100, 100, 255, clampAlpha(fieldIntensity * 0.2f));
                    RoundedUtils.drawRound(x, y, 2, 2, 1, fieldColor);
                }
            }
        } catch (Exception e) {
            System.err.println("Error drawing electric background");
            e.printStackTrace();
        }
    }

    private void drawElectricGrid(float time) {
        try {
            Color gridColor = new Color(0, 255, 255, 30);
            float spacing = 50f;

            for (float x = 0; x < this.width; x += spacing) {
                float alpha = (float) Math.max(0.1f, 0.3f + 0.2f * Math.sin(time * 2f + x * 0.01f));
                Color lineColor = new Color(0, 255, 255, clampAlpha(alpha));
                RoundedUtils.drawRound(x, 0, 1, this.height, 0, lineColor);
            }

            for (float y = 0; y < this.height; y += spacing) {
                float alpha = (float) Math.max(0.1f, 0.3f + 0.2f * Math.sin(time * 2f + y * 0.01f));
                Color lineColor = new Color(0, 255, 255, clampAlpha(alpha));
                RoundedUtils.drawRound(0, y, this.width, 1, 0, lineColor);
            }
        } catch (Exception e) {
            System.err.println("Error drawing electric grid");
            e.printStackTrace();
        }
    }

    private void drawNeonParticles(float time, Color color1, Color color2) {
        try {
            for (int i = 0; i < 20; i++) {
                float x = (float) (this.width * 0.1f + (this.width * 0.8f) * Math.sin(time * 0.5f + i * 0.5f));
                float y = (float) (this.height * 0.1f + (this.height * 0.8f) * Math.cos(time * 0.3f + i * 0.7f));

                float alpha = (float) Math.max(0.2f, 0.6f + 0.4f * Math.sin(time * 3f + i));
                Color particleColor = i % 2 == 0 ?
                        new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), clampAlpha(alpha)) :
                        new Color(color2.getRed(), color2.getGreen(), color2.getBlue(), clampAlpha(alpha));

                float size = (float) Math.max(2, 4 + 2 * Math.sin(time * 2f + i));
                RoundedUtils.drawRound(x - size/2, y - size/2, size, size, size/2, particleColor);
            }
        } catch (Exception e) {
            System.err.println("Error drawing neon particles");
            e.printStackTrace();
        }
    }

    private void drawScanLines(float time) {
        try {
            Color scanColor = new Color(0, 255, 255, 20);
            float scanY = (float) ((time * 100) % (this.height + 50));

            for (int i = 0; i < 3; i++) {
                float y = scanY - i * 15;
                if (y >= -5 && y <= this.height + 5) {
                    RoundedUtils.drawRound(0, y, this.width, 2, 0, scanColor);
                }
            }
        } catch (Exception e) {
            System.err.println("Error drawing scan lines");
            e.printStackTrace();
        }
    }

    private void drawStarField(float time) {
        try {
            for (int i = 0; i < 100; i++) {
                float x = (i * 73.2f) % this.width;
                float y = (i * 127.7f) % this.height;

                float twinkle = (float) Math.max(0.3f, 0.8f + 0.5f * Math.sin(time * 3f + i * 0.1f));
                Color starColor = new Color(255, 255, 255, clampAlpha(twinkle));

                RoundedUtils.drawRound(x, y, 1, 1, 0, starColor);
            }
        } catch (Exception e) {
            System.err.println("Error drawing star field");
            e.printStackTrace();
        }
    }

    private void drawNebulaClouds(float time) {
        try {
            for (int i = 0; i < 5; i++) {
                float x = (float) (this.width * 0.2f * i + 50 * Math.sin(time * 0.2f + i));
                float y = (float) (this.height * 0.3f + 30 * Math.cos(time * 0.15f + i));
                float size = 80 + 20 * i;

                Color nebulaColor = new Color(100, 50, 150, 15);
                RoundedUtils.drawRound(x - size/2, y - size/2, size, size, size/2, nebulaColor);
            }
        } catch (Exception e) {
            System.err.println("Error drawing nebula clouds");
            e.printStackTrace();
        }
    }

    private void drawTwinklingStars(float time) {
        try {
            for (int i = 0; i < 30; i++) {
                float x = (i * 91.3f) % this.width;
                float y = (i * 157.1f) % this.height;

                float size = (float) Math.max(1, 2 + Math.sin(time * 4f + i * 0.2f));
                float alpha = (float) Math.max(0.4f, 0.8f + 0.4f * Math.sin(time * 2f + i * 0.3f));

                Color starColor = new Color(200, 200, 255, clampAlpha(alpha));
                RoundedUtils.drawRound(x - size/2, y - size/2, size, size, size/2, starColor);
            }
        } catch (Exception e) {
            System.err.println("Error drawing twinkling stars");
            e.printStackTrace();
        }
    }

    private void drawNeonStreaks(float time) {
        try {
            for (int i = 0; i < 8; i++) {
                float x = (float) (this.width * 0.1f + (this.width * 0.8f) * ((i * 123.456f) % 1.0f));
                float y = (float) (time * 50 + i * 30) % (this.height + 100);

                Color streakColor = new Color(255, 100, 200, 40);
                RoundedUtils.drawRound(x, y, 2, 20, 1, streakColor);
            }
        } catch (Exception e) {
            System.err.println("Error drawing neon streaks");
            e.printStackTrace();
        }
    }

    private void drawCityGlow(float time) {
        try {
            float centerX = this.width / 2f;
            float centerY = this.height * 0.8f;

            for (int i = 0; i < 3; i++) {
                float radius = 100 + i * 50;
                float alpha = 0.1f - i * 0.02f;
                Color glowColor = new Color(255, 150, 0, clampAlpha(alpha));

                RoundedUtils.drawRound(centerX - radius, centerY - radius,
                        radius * 2, radius * 2, radius, glowColor);
            }
        } catch (Exception e) {
            System.err.println("Error drawing city glow");
            e.printStackTrace();
        }
    }

    private void drawDigitalRain(float time) {
        try {
            for (int i = 0; i < 15; i++) {
                float x = (i * 80f) % this.width;
                float y = (float) ((time * 150 + i * 50) % (this.height + 100));

                Color rainColor = new Color(0, 255, 200, 60);
                RoundedUtils.drawRound(x, y, 1, 15, 0, rainColor);
            }
        } catch (Exception e) {
            System.err.println("Error drawing digital rain");
            e.printStackTrace();
        }
    }

    private void drawAuroraWaves(float time) {
        try {
            for (int wave = 0; wave < 3; wave++) {
                Color waveColor = new Color(100 + wave * 30, 255 - wave * 40, 150 + wave * 20, 30);

                for (int x = 0; x < this.width; x += 5) {
                    float waveHeight = (float) (Math.sin((x * 0.01f) + time * 0.5f + wave) * 30);
                    float y = this.height * 0.3f + waveHeight + wave * 20;

                    RoundedUtils.drawRound(x, y, 5, 8, 2, waveColor);
                }
            }
        } catch (Exception e) {
            System.err.println("Error drawing aurora waves");
            e.printStackTrace();
        }
    }

    private void drawShimmeringParticles(float time) {
        try {
            for (int i = 0; i < 25; i++) {
                float x = (float) (this.width * 0.5f + this.width * 0.4f * Math.sin(time * 0.3f + i * 0.1f));
                float y = (float) (this.height * 0.4f + 50 * Math.cos(time * 0.2f + i * 0.15f));

                float alpha = (float) Math.max(0.2f, 0.6f + 0.4f * Math.sin(time * 4f + i));
                Color shimmerColor = new Color(150, 255, 200, clampAlpha(alpha));

                RoundedUtils.drawRound(x, y, 2, 2, 1, shimmerColor);
            }
        } catch (Exception e) {
            System.err.println("Error drawing shimmering particles");
            e.printStackTrace();
        }
    }

    private void drawMatrixRain(float time) {
        try {
            for (int i = 0; i < 20; i++) {
                float x = (i * 40f) % this.width;
                float y = (float) ((time * 100 + i * 30) % (this.height + 80));

                Color matrixColor = new Color(0, 255, 0, 80);
                RoundedUtils.drawRound(x, y, 2, 12, 0, matrixColor);
            }
        } catch (Exception e) {
            System.err.println("Error drawing matrix rain");
            e.printStackTrace();
        }
    }

    private void drawMatrixGlow(float time) {
        try {
            Color glowColor = new Color(0, 255, 0, 20);
            float glowY = (float) ((time * 80) % (this.height + 40));

            RoundedUtils.drawRound(0, glowY, this.width, 3, 0, glowColor);
        } catch (Exception e) {
            System.err.println("Error drawing matrix glow");
            e.printStackTrace();
        }
    }

    private void drawRetroGrid(float time) {
        try {
            Color gridColor = new Color(255, 0, 255, 40);
            float spacing = 40f;

            for (float x = 0; x < this.width; x += spacing) {
                RoundedUtils.drawRound(x, this.height * 0.6f, 1, this.height * 0.4f, 0, gridColor);
            }

            for (float y = this.height * 0.6f; y < this.height; y += spacing * 0.5f) {
                RoundedUtils.drawRound(0, y, this.width, 1, 0, gridColor);
            }
        } catch (Exception e) {
            System.err.println("Error drawing retro grid");
            e.printStackTrace();
        }
    }

    private void drawRetroSun(float time) {
        try {
            float centerX = this.width / 2f;
            float centerY = this.height * 0.3f;

            Color sunColor = new Color(255, 100, 0, 100);
            RoundedUtils.drawRound(centerX - 30, centerY - 30, 60, 60, 30, sunColor);

            for (int i = 0; i < 8; i++) {
                float angle = (float) (i * Math.PI / 4 + time * 0.5f);
                float rayX = (float) (centerX + Math.cos(angle) * 50);
                float rayY = (float) (centerY + Math.sin(angle) * 50);

                Color rayColor = new Color(255, 150, 0, 60);
                RoundedUtils.drawRound(rayX, rayY, 20, 2, 1, rayColor);
            }
        } catch (Exception e) {
            System.err.println("Error drawing retro sun");
            e.printStackTrace();
        }
    }

    private void drawSynthWaves(float time) {
        try {
            for (int wave = 0; wave < 4; wave++) {
                Color waveColor = new Color(255 - wave * 30, wave * 50, 255, 50);

                for (int x = 0; x < this.width; x += 3) {
                    float waveHeight = (float) (Math.sin((x * 0.005f) + time + wave * 0.5f) * 15);
                    float y = this.height * 0.7f + waveHeight + wave * 10;

                    RoundedUtils.drawRound(x, y, 3, 3, 1, waveColor);
                }
            }
        } catch (Exception e) {
            System.err.println("Error drawing synth waves");
            e.printStackTrace();
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        try {
            for (MenuButton button : menuOptions) {
                if (button != null) {
                    button.mouseClicked(mouseX, mouseY, mouseButton);
                }
            }

            if (backgroundButton != null) {
                backgroundButton.mouseClicked(mouseX, mouseY, mouseButton);
            }
        } catch (Exception e) {
            System.err.println("Error handling mouse click");
            e.printStackTrace();
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        try {
            switch (keyCode) {
                case 82:
                    currentBackgroundStyle = BackgroundStyle.CYBERPUNK;
                    updateBackgroundButton();
                    break;
                case 68:
                    currentBackgroundStyle = BackgroundStyle.DISABLED;
                    updateBackgroundButton();
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error handling key typed");
            e.printStackTrace();
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }


    public static void setBackgroundStyle(BackgroundStyle style) {
        currentBackgroundStyle = style;
    }

    public static void setCustomSpeed(float speed) {
        customSpeed = speed;
        useCustomSpeed = true;
    }

    public static BackgroundStyle getCurrentBackgroundStyle() {
        return currentBackgroundStyle;
    }


    public enum BackgroundStyle {
        DISABLED("None"),
        CYBERPUNK("Cyberpunk"),
        GALAXY("Galaxy"),
        NEON_CITY("Neon City"),
        AURORA("Aurora"),
        MATRIX("Matrix"),
        RETRO_WAVE("Retro Wave"),
        PLASMA("Plasma"),
        FIRE("Fire"),
        OCEAN("Ocean"),
        RAINBOW("Rainbow"),
        DIGITAL("Digital"),
        SPACE_WARP("Space Warp"),
        ELECTRIC("Electric");

        @Getter
        private final String displayName;

        BackgroundStyle(String displayName) {
            this.displayName = displayName;
        }
    }

    public static class ChangeLogEntry {
        @Getter
        private final String details;
        @Getter
        private final ChangeLogEntryType type;

        public ChangeLogEntry(String details, ChangeLogEntryType type) {
            this.details = details;
            this.type = type;
        }
    }

    public enum ChangeLogEntryType {
        ADDITION("[+]", new Color(54, 239, 61).getRGB()),
        FIX("[~]", new Color(255, 225, 99).getRGB()),
        IMPROVEMENT("[*]", new Color(103, 241, 114).getRGB()),
        REMOVAL("[-]", new Color(255, 64, 64).getRGB()),
        OTHER("[?]", new Color(180, 72, 180).getRGB());

        @Getter
        private final String prefix;
        @Getter
        private final int textColor;

        ChangeLogEntryType(String prefix, int textColor) {
            this.prefix = prefix;
            this.textColor = textColor;
        }
    }
}