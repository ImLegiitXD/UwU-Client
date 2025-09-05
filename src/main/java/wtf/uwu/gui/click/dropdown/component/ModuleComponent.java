package wtf.uwu.gui.click.dropdown.component;

import lombok.Getter;
import lombok.Setter;
import wtf.uwu.UwU;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.impl.visual.ClickGUI;
import wtf.uwu.features.values.Value;
import wtf.uwu.features.values.impl.*;
import wtf.uwu.gui.click.Component;
import wtf.uwu.gui.click.IComponent;
import wtf.uwu.gui.click.dropdown.component.impl.*;
import wtf.uwu.gui.font.Fonts;
import wtf.uwu.utils.animations.Direction;
import wtf.uwu.utils.animations.impl.EaseInOutQuad;
import wtf.uwu.utils.animations.impl.EaseOutSine;
import wtf.uwu.utils.render.ColorUtils;
import wtf.uwu.utils.render.MouseUtils;
import wtf.uwu.utils.render.RenderUtils;
import wtf.uwu.utils.render.RoundedUtils;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@Setter
public class ModuleComponent implements IComponent {
    private static final float MODULE_HEIGHT = 22f;
    private static final float BORDER_RADIUS = 8f;
    private static final float SETTING_MARGIN = 3f;

    private float x, y, width, height = MODULE_HEIGHT;
    private final Module module;
    private final CopyOnWriteArrayList<Component> settings = new CopyOnWriteArrayList<>();
    private boolean opened;

    private final EaseInOutQuad openAnimation = new EaseInOutQuad(280, 1);
    private final EaseOutSine toggleAnimation = new EaseOutSine(320, 1);
    private final EaseInOutQuad hoverAnimation = new EaseInOutQuad(180, 1);

    public ModuleComponent(Module module) {
        this.module = module;
        openAnimation.setDirection(Direction.BACKWARDS);
        toggleAnimation.setDirection(Direction.BACKWARDS);
        hoverAnimation.setDirection(Direction.BACKWARDS);

        initializeSettings();
    }

    private void initializeSettings() {
        for (Value value : module.getValues()) {
            if (value instanceof BoolValue boolValue) {
                settings.add(new BooleanComponent(boolValue));
            }
            if (value instanceof ColorValue colorValue) {
                settings.add(new ColorPickerComponent(colorValue));
            }
            if (value instanceof SliderValue sliderValue) {
                settings.add(new SliderComponent(sliderValue));
            }
            if (value instanceof ModeValue modeValue) {
                settings.add(new ModeComponent(modeValue));
            }
            if (value instanceof MultiBoolValue multiBoolValue) {
                settings.add(new MultiBooleanComponent(multiBoolValue));
            }
            if (value instanceof TextValue textValue) {
                settings.add(new StringComponent(textValue));
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        updateAnimations(mouseX, mouseY);

        Color mainColor = UwU.INSTANCE.getModuleManager().getModule(ClickGUI.class).color.get();

        renderModuleBackground(mainColor);
        renderModuleContent(mainColor);
        renderSettings(mouseX, mouseY, mainColor);

        IComponent.super.drawScreen(mouseX, mouseY);
    }

    private void updateAnimations(int mouseX, int mouseY) {
        openAnimation.setDirection(opened ? Direction.FORWARDS : Direction.BACKWARDS);
        toggleAnimation.setDirection(module.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
        hoverAnimation.setDirection(isHovered(mouseX, mouseY) ? Direction.FORWARDS : Direction.BACKWARDS);
    }

    private void renderModuleBackground(Color mainColor) {
        float backgroundHeight = MODULE_HEIGHT - 2;

        if (module.isEnabled()) {
            RoundedUtils.drawGradientRound(
                    x - 0.5f, y - 0.5f, width + 1, backgroundHeight + 1,
                    BORDER_RADIUS + 0.5f,
                    ColorUtils.reAlpha(ColorUtils.darker(mainColor, 0.8f), (int)(60 + 20 * hoverAnimation.getOutput())),
                    ColorUtils.reAlpha(ColorUtils.darker(mainColor, 0.6f), (int)(40 + 15 * hoverAnimation.getOutput())),
                    ColorUtils.reAlpha(ColorUtils.darker(mainColor, 0.8f), (int)(60 + 20 * hoverAnimation.getOutput())),
                    ColorUtils.reAlpha(ColorUtils.darker(mainColor, 0.6f), (int)(40 + 15 * hoverAnimation.getOutput()))
            );
        }

        Color backgroundColor = getBackgroundColor(mainColor);
        RoundedUtils.drawRound(x, y, width, backgroundHeight, BORDER_RADIUS, backgroundColor);

        if (module.isEnabled()) {
            Color borderColor = ColorUtils.reAlpha(ColorUtils.brighter(mainColor, 0.2f),
                    (int)(100 + 50 * hoverAnimation.getOutput()));
            RoundedUtils.drawRoundOutline(x, y, width, backgroundHeight, BORDER_RADIUS, 1.2f, borderColor, borderColor);
        }

        if (hoverAnimation.getOutput() > 0.1) {
            Color glowColor = ColorUtils.reAlpha(module.isEnabled() ? mainColor : Color.WHITE,
                    (int)(30 * hoverAnimation.getOutput()));
            RoundedUtils.drawRound(x, y, width, backgroundHeight, BORDER_RADIUS, glowColor);
        }
    }

    private Color getBackgroundColor(Color mainColor) {
        if (module.isEnabled()) {
            float intensity = (float)(0.4f + 0.3f * toggleAnimation.getOutput() + 0.2f * hoverAnimation.getOutput());
            return ColorUtils.reAlpha(ColorUtils.interpolateColorC(mainColor, Color.WHITE, 0.1f),
                    (int)(140 + 60 * intensity));
        } else {
            Color baseGray = new Color(45, 45, 50);
            float hoverIntensity = (float)(0.3f * hoverAnimation.getOutput());
            return ColorUtils.reAlpha(ColorUtils.interpolateColorC(baseGray, mainColor, hoverIntensity),
                    (int)(120 + 40 * hoverAnimation.getOutput()));
        }
    }

    private void renderModuleContent(Color mainColor) {
        float textY = y + MODULE_HEIGHT / 2 - 5f;

        Color textColor = getTextColor(mainColor);

        float textScale = (float)(1.0f + 0.05f * hoverAnimation.getOutput());
        RenderUtils.scaleStart(x + width / 2, textY + 4, textScale);

        Fonts.interMedium.get(14).drawCenteredString(
                module.getName(),
                x + width / 2,
                textY,
                textColor.getRGB()
        );

        RenderUtils.scaleEnd();

        renderStatusIndicators(mainColor);
    }

    private Color getTextColor(Color mainColor) {
        if (module.isEnabled()) {
            Color baseColor = ColorUtils.interpolateColorC(Color.WHITE, mainColor, 0.3f);
            return ColorUtils.interpolateColorC(baseColor, Color.WHITE, (float)hoverAnimation.getOutput() * 0.2f);
        } else {
            Color baseGray = new Color(160, 160, 165);
            return ColorUtils.interpolateColorC(baseGray, Color.WHITE, (float)hoverAnimation.getOutput() * 0.4f);
        }
    }

    private void renderStatusIndicators(Color mainColor) {
        if (module.isEnabled()) {
            float dotSize = (float)(2.5f + 0.8f * toggleAnimation.getOutput());
            Color dotColor = ColorUtils.interpolateColorC(mainColor, Color.WHITE, 0.4f);
            RenderUtils.drawCircle(
                    x + 6, y + MODULE_HEIGHT / 2 - 1, 0, 360,
                    dotSize, 0.1f, true,
                    ColorUtils.reAlpha(dotColor, (int)(200 + 55 * toggleAnimation.getOutput())).getRGB()
            );
        }

        if (!settings.isEmpty()) {
            float indicatorX = x + width - 10;
            float indicatorY = y + MODULE_HEIGHT / 2 - 1;

            float rotation = (float)(opened ? 90 * openAnimation.getOutput() : 0);

            RenderUtils.pushMatrix();
            RenderUtils.translate(indicatorX, indicatorY, 0);
            RenderUtils.rotate(rotation, 0, 0, 1);

            Color iconColor = module.isEnabled() ?
                    ColorUtils.interpolateColorC(mainColor, Color.WHITE, 0.5f) :
                    new Color(120, 120, 125);

            Fonts.interRegular.get(10).drawCenteredString("⚙", 0, -2, iconColor.getRGB());

            RenderUtils.popMatrix();
        }
    }

    private void renderSettings(int mouseX, int mouseY, Color mainColor) {
        float settingsOffsetY = MODULE_HEIGHT + SETTING_MARGIN;

        for (Component component : settings) {
            if (!component.isVisible()) continue;

            component.setX(x + SETTING_MARGIN);
            component.setY((float) (y + settingsOffsetY * openAnimation.getOutput()));
            component.setWidth(width - SETTING_MARGIN * 2);

            if (openAnimation.getOutput() > 0.6f) {
                Color settingBg = getSettingBackgroundColor(mainColor);

                RoundedUtils.drawRound(
                        component.getX() - 1, component.getY() - 1,
                        component.getWidth() + 2, component.getHeight() + 2,
                        BORDER_RADIUS - 2, settingBg
                );

                float alpha = (float)Math.min(1.0, (openAnimation.getOutput() - 0.6f) / 0.4f);
                RenderUtils.setAlphaLimit(alpha);

                component.drawScreen(mouseX, mouseY);

                RenderUtils.setAlphaLimit(1.0f);
            }

            settingsOffsetY += (float) (component.getHeight() * openAnimation.getOutput());
        }

        this.height = settingsOffsetY + SETTING_MARGIN;
    }

    private Color getSettingBackgroundColor(Color mainColor) {
        if (module.isEnabled()) {
            return ColorUtils.reAlpha(ColorUtils.darker(mainColor, 0.85f), 100);
        } else {
            return ColorUtils.reAlpha(new Color(35, 35, 40), 120);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHovered(mouseX, mouseY)) {
            switch (mouseButton) {
                case 0:
                    module.toggle();
                    break;
                case 1:
                    if (!settings.isEmpty()) {
                        opened = !opened;
                    }
                    break;
            }
        }

        if (opened && openAnimation.getOutput() > 0.5) {
            settings.forEach(setting -> setting.mouseClicked(mouseX, mouseY, mouseButton));
        }

        IComponent.super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (opened) {
            settings.forEach(setting -> setting.mouseReleased(mouseX, mouseY, state));
        }
        IComponent.super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (opened) {
            settings.forEach(setting -> setting.keyTyped(typedChar, keyCode));
        }
        IComponent.super.keyTyped(typedChar, keyCode);
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return MouseUtils.isHovered2(x, y, width, MODULE_HEIGHT - 2, mouseX, mouseY);
    }
}