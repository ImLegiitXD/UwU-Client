package wtf.uwu.gui.click.dropdown.component.impl;

import wtf.uwu.features.modules.impl.visual.ClickGUI;
import wtf.uwu.features.values.impl.BoolValue;
import wtf.uwu.gui.click.Component;
import wtf.uwu.gui.font.Fonts;
import wtf.uwu.utils.animations.Direction;
import wtf.uwu.utils.animations.impl.EaseInOutQuad;
import wtf.uwu.utils.render.ColorUtils;
import wtf.uwu.utils.render.MouseUtils;
import wtf.uwu.utils.render.RenderUtils;
import wtf.uwu.utils.render.RoundedUtils;

import java.awt.*;

public class BooleanComponent extends Component {
    private final BoolValue setting;
    private final EaseInOutQuad toggleAnimation = new EaseInOutQuad(250, 1);
    private final EaseInOutQuad hoverAnimation = new EaseInOutQuad(200, 1);

    private static final float SWITCH_WIDTH = 22f;
    private static final float SWITCH_HEIGHT = 12f;
    private static final float THUMB_SIZE = 8f;

    public BooleanComponent(BoolValue setting) {
        this.setting = setting;
        setHeight(Fonts.interRegular.get(15).getHeight() + 8);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        toggleAnimation.setDirection(this.setting.get() ? Direction.FORWARDS : Direction.BACKWARDS);
        boolean isHovered = MouseUtils.isHovered2(getX(), getY(), getWidth(), getHeight(), mouseX, mouseY);
        hoverAnimation.setDirection(isHovered ? Direction.FORWARDS : Direction.BACKWARDS);

        Color textColor = ColorUtils.interpolateColorC(new Color(200, 200, 200), Color.WHITE, (float) hoverAnimation.getOutput());
        Fonts.interMedium.get(15).drawString(setting.getName(), getX() + 4, getY() + getHeight() / 2f - 4, textColor.getRGB());

        float switchX = getX() + getWidth() - SWITCH_WIDTH - 4;
        float switchY = getY() + getHeight() / 2f - SWITCH_HEIGHT / 2f;

        Color trackColor = ColorUtils.interpolateColorC(
                new Color(80, 80, 85),
                INSTANCE.getModuleManager().getModule(ClickGUI.class).color.get(),
                (float) toggleAnimation.getOutput()
        );

        RoundedUtils.drawRound(switchX, switchY, SWITCH_WIDTH, SWITCH_HEIGHT, SWITCH_HEIGHT / 2f, trackColor);

        float thumbX = switchX + THUMB_SIZE / 2f - 2f + (SWITCH_WIDTH - THUMB_SIZE) * (float) toggleAnimation.getOutput();
        Color thumbColor = ColorUtils.interpolateColorC(new Color(220, 220, 220), Color.WHITE, (float) hoverAnimation.getOutput());
        RoundedUtils.drawRound(thumbX, switchY + SWITCH_HEIGHT / 2f - THUMB_SIZE / 2f, THUMB_SIZE, THUMB_SIZE, THUMB_SIZE / 2f, thumbColor);

        super.drawScreen(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (MouseUtils.isHovered2(getX(), getY(), getWidth(), getHeight(), mouseX, mouseY) && mouseButton == 0) {
            this.setting.set(!this.setting.get());
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean isVisible() {
        return this.setting.canDisplay();
    }
}