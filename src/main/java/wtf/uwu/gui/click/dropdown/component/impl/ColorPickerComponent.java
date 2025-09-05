package wtf.uwu.gui.click.dropdown.component.impl;

import wtf.uwu.features.values.impl.ColorValue;
import wtf.uwu.gui.click.Component;
import wtf.uwu.gui.font.Fonts;
import wtf.uwu.utils.animations.Animation;
import wtf.uwu.utils.animations.Direction;
import wtf.uwu.utils.animations.impl.EaseOutSine;
import wtf.uwu.utils.render.MouseUtils;
import wtf.uwu.utils.render.RenderUtils;
import wtf.uwu.utils.render.RoundedUtils;

import java.awt.*;

public class ColorPickerComponent extends Component {
    private final ColorValue setting;
    private final Animation openAnimation = new EaseOutSine(300, 1);
    private boolean opened, pickingHue, pickingSB, pickingAlpha;

    private static final float HEADER_HEIGHT = 16f;
    private static final float PADDING = 4f;
    private static final float SB_BOX_HEIGHT = 60f;
    private static final float SLIDER_HEIGHT = 8f;

    public ColorPickerComponent(ColorValue setting) {
        this.setting = setting;
        openAnimation.setDirection(Direction.BACKWARDS);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        updatePicker(mouseX, mouseY);
        openAnimation.setDirection(opened ? Direction.FORWARDS : Direction.BACKWARDS);

        float openFactor = (float) openAnimation.getOutput();
        float pickerHeight = (SB_BOX_HEIGHT + SLIDER_HEIGHT * 2 + PADDING * 3) * openFactor;
        setHeight(HEADER_HEIGHT + pickerHeight);

        renderHeader();

        if (openFactor > 0) {
            RenderUtils.setAlphaLimit(openFactor);
            renderSaturationBrightnessBox();
            renderHueSlider();
            renderAlphaSlider();
            RenderUtils.setAlphaLimit(1.0f);
        }

        super.drawScreen(mouseX, mouseY);
    }

    private void renderHeader() {
        Fonts.interMedium.get(15).drawString(setting.getName(), getX() + PADDING, getY() + HEADER_HEIGHT / 2f - 4f, -1);
        RoundedUtils.drawRound(getX() + getWidth() - HEADER_HEIGHT - PADDING, getY() + 2, HEADER_HEIGHT, HEADER_HEIGHT - 4, 4, setting.get());
    }

    private void renderSaturationBrightnessBox() {
        float boxX = getX() + PADDING;
        float boxY = getY() + HEADER_HEIGHT + PADDING;
        float boxWidth = getWidth() - PADDING * 2;

        RoundedUtils.drawGradientRound(boxX, boxY, boxWidth, SB_BOX_HEIGHT, 4, Color.WHITE, Color.getHSBColor(setting.getHue(), 1, 1), Color.BLACK, Color.BLACK);

        float sat = setting.getSaturation();
        float bri = setting.getBrightness();
        float handleX = boxX + boxWidth * sat;
        float handleY = boxY + SB_BOX_HEIGHT * (1 - bri);
        RenderUtils.drawCircle(handleX, handleY, 0, 360, 3.5f, 0.1f, false, Color.BLACK.getRGB());
        RenderUtils.drawCircle(handleX, handleY, 0, 360, 2.5f, 0.1f, false, Color.WHITE.getRGB());
    }

    private void renderHueSlider() {
        float sliderX = getX() + PADDING;
        float sliderY = getY() + HEADER_HEIGHT + SB_BOX_HEIGHT + PADDING * 2;
        float sliderWidth = getWidth() - PADDING * 2;

        for (int i = 0; i < sliderWidth; i++) {
            RenderUtils.drawRect(sliderX + i, sliderY, sliderX + i + 1, sliderY + SLIDER_HEIGHT, Color.getHSBColor(i / sliderWidth, 1, 1).getRGB());
        }
        RoundedUtils.drawRoundOutline(sliderX, sliderY, sliderWidth, SLIDER_HEIGHT, 4f, 1f, new Color(0,0,0,50), new Color(0,0,0,50));

        float handleX = sliderX + sliderWidth * setting.getHue();
        RoundedUtils.drawRound(handleX - 2, sliderY - 1, 4, SLIDER_HEIGHT + 2, 2, Color.WHITE);
    }

    private void renderAlphaSlider() {
        float sliderX = getX() + PADDING;
        float sliderY = getY() + HEADER_HEIGHT + SB_BOX_HEIGHT + SLIDER_HEIGHT + PADDING * 3;
        float sliderWidth = getWidth() - PADDING * 2;

        drawCheckerboard(sliderX, sliderY, sliderWidth, SLIDER_HEIGHT);

        Color colorNoAlpha = new Color(setting.get().getRed(), setting.get().getGreen(), setting.get().getBlue());
        RoundedUtils.drawGradientHorizontal(sliderX, sliderY, sliderWidth, SLIDER_HEIGHT, 4f, new Color(colorNoAlpha.getRed(), colorNoAlpha.getGreen(), colorNoAlpha.getBlue(), 0), colorNoAlpha);
        RoundedUtils.drawRoundOutline(sliderX, sliderY, sliderWidth, SLIDER_HEIGHT, 4f, 1f, new Color(0,0,0,50), new Color(0,0,0,50));

        float handleX = sliderX + sliderWidth * setting.getAlpha();
        RoundedUtils.drawRound(handleX - 2, sliderY - 1, 4, SLIDER_HEIGHT + 2, 2, Color.WHITE);
    }

    private void updatePicker(int mouseX, int mouseY) {
        if (!opened) return;

        float boxX = getX() + PADDING;
        float boxY = getY() + HEADER_HEIGHT + PADDING;
        float boxWidth = getWidth() - PADDING * 2;

        if (pickingSB) {
            float sat = Math.max(0, Math.min(1, (mouseX - boxX) / boxWidth));
            float bri = Math.max(0, Math.min(1, 1 - (mouseY - boxY) / SB_BOX_HEIGHT));
            setting.setSaturation(sat);
            setting.setBrightness(bri);
        }

        float hueSliderY = getY() + HEADER_HEIGHT + SB_BOX_HEIGHT + PADDING * 2;
        if (pickingHue) {
            float hue = Math.max(0, Math.min(1, (mouseX - boxX) / boxWidth));
            setting.setHue(hue);
        }

        float alphaSliderY = getY() + HEADER_HEIGHT + SB_BOX_HEIGHT + SLIDER_HEIGHT + PADDING * 3;
        if (pickingAlpha) {
            float alpha = Math.max(0, Math.min(1, (mouseX - boxX) / boxWidth));
            setting.setAlpha(alpha);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        if (MouseUtils.isHovered2(getX() + getWidth() - HEADER_HEIGHT - PADDING, getY(), HEADER_HEIGHT + PADDING, HEADER_HEIGHT, mouseX, mouseY)) {
            opened = !opened;
        }

        if (opened && mouseButton == 0) {
            float boxX = getX() + PADDING;
            float boxY = getY() + HEADER_HEIGHT + PADDING;

            if (MouseUtils.isHovered2(boxX, boxY, getWidth() - PADDING * 2, SB_BOX_HEIGHT, mouseX, mouseY)) {
                pickingSB = true;
            }

            float hueSliderY = getY() + HEADER_HEIGHT + SB_BOX_HEIGHT + PADDING * 2;

            if (MouseUtils.isHovered2(boxX, hueSliderY, getWidth() - PADDING * 2, SLIDER_HEIGHT, mouseX, mouseY)) {
                pickingHue = true;
            }

            float alphaSliderY = getY() + HEADER_HEIGHT + SB_BOX_HEIGHT + SLIDER_HEIGHT + PADDING * 3;
            if (MouseUtils.isHovered2(boxX, alphaSliderY, getWidth() - PADDING * 2, SLIDER_HEIGHT, mouseX, mouseY)) {
                pickingAlpha = true;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == 0) {
            pickingHue = pickingSB = pickingAlpha = false;
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    private void drawCheckerboard(float x, float y, float width, float height) {
        RenderUtils.drawRect(x, y, x + width, y + height, new Color(200, 200, 200).getRGB());
        int squareSize = 4;
        for (float i = 0; i < width; i += squareSize * 2) {
            for (float j = 0; j < height; j += squareSize * 2) {
                RenderUtils.drawRect(x + i, y + j, x + i + squareSize, y + j + squareSize, Color.LIGHT_GRAY.getRGB());
                RenderUtils.drawRect(x + i + squareSize, y + j + squareSize, x + i + squareSize * 2, y + j + squareSize*2, Color.LIGHT_GRAY.getRGB());
            }
        }
    }

    @Override
    public boolean isVisible() {
        return this.setting.canDisplay();
    }
}