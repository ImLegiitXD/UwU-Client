package wtf.uwu.gui.click.dropdown.component.impl;

import net.minecraft.util.MathHelper;
import wtf.uwu.features.modules.impl.visual.ClickGUI;
import wtf.uwu.features.values.impl.SliderValue;
import wtf.uwu.gui.click.Component;
import wtf.uwu.gui.font.Fonts;
import wtf.uwu.utils.math.MathUtils;
import wtf.uwu.utils.render.MouseUtils;
import wtf.uwu.utils.render.RenderUtils;
import wtf.uwu.utils.render.RoundedUtils;

import java.awt.*;
import java.text.DecimalFormat;

public class SliderComponent extends Component {
    private final SliderValue setting;
    private boolean dragging;
    private final DecimalFormat df = new DecimalFormat("0.##");

    public SliderComponent(SliderValue setting) {
        this.setting = setting;
        setHeight(Fonts.interRegular.get(14).getHeight() + 14);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        if (dragging) {
            double difference = setting.getMax() - setting.getMin();
            double value = setting.getMin() + MathHelper.clamp_double((mouseX - (getX() + 4)) / (getWidth() - 8), 0, 1) * difference;
            setting.setValue((float) MathUtils.incValue(value, setting.getIncrement()));
        }

        Fonts.interMedium.get(14).drawString(setting.getName(), getX() + 4, getY() + 2, Color.WHITE.getRGB());
        String valueStr = df.format(setting.get());
        Fonts.interRegular.get(14).drawString(valueStr, getX() + getWidth() - Fonts.interRegular.get(14).getStringWidth(valueStr) - 4, getY() + 2, new Color(200, 200, 200).getRGB());

        float sliderY = getY() + Fonts.interRegular.get(14).getHeight() + 6;
        float sliderX = getX() + 4;
        float sliderWidth = getWidth() - 8;
        float sliderHeight = 4f;

        RoundedUtils.drawRound(sliderX, sliderY, sliderWidth, sliderHeight, sliderHeight / 2f, new Color(40, 40, 45));

        float filledWidth = (float) ((sliderWidth) * (setting.get() - setting.getMin()) / (setting.getMax() - setting.getMin()));
        if (filledWidth > 0) {
            Color startColor = INSTANCE.getModuleManager().getModule(ClickGUI.class).color.get();
            Color endColor = new Color(startColor.getRed(), startColor.getGreen(), startColor.getBlue(), 180);
            RoundedUtils.drawGradientHorizontal(sliderX, sliderY, filledWidth, sliderHeight, sliderHeight / 2f, startColor, endColor);
        }

        float handleX = sliderX + filledWidth;
        float handleRadius = 4f;
        boolean isHovered = MouseUtils.isHovered2(sliderX, sliderY - 2, sliderWidth, sliderHeight + 4, mouseX, mouseY);
        if (isHovered || dragging) {
            handleRadius = 5f;
        }
        RenderUtils.drawCircle(handleX, sliderY + sliderHeight / 2f, 0, 360, handleRadius, 0.1f, true, Color.WHITE.getRGB());
        RenderUtils.drawCircle(handleX, sliderY + sliderHeight / 2f, 0, 360, handleRadius - 1f, 0.1f, true, INSTANCE.getModuleManager().getModule(ClickGUI.class).color.get().getRGB());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && MouseUtils.isHovered2(getX(), getY() + getHeight() - 10, getWidth(), 12, mouseX, mouseY)) {
            dragging = true;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == 0) dragging = false;
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public boolean isVisible() {
        return this.setting.canDisplay();
    }
}