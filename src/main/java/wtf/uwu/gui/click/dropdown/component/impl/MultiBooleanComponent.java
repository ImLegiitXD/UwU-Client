package wtf.uwu.gui.click.dropdown.component.impl;

import wtf.uwu.features.modules.impl.visual.ClickGUI;
import wtf.uwu.features.values.impl.BoolValue;
import wtf.uwu.features.values.impl.MultiBoolValue;
import wtf.uwu.gui.click.Component;
import wtf.uwu.gui.font.Fonts;
import wtf.uwu.utils.render.ColorUtils;
import wtf.uwu.utils.render.MouseUtils;
import wtf.uwu.utils.render.RoundedUtils;

import java.awt.*;

public class MultiBooleanComponent extends Component {
    private final MultiBoolValue setting;

    private static final float PADDING = 4f;
    private static final float OPTION_HEIGHT = 14f;

    public MultiBooleanComponent(MultiBoolValue setting) {
        this.setting = setting;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        Fonts.interMedium.get(15).drawString(setting.getName(), getX() + 4, getY(), Color.WHITE.getRGB());

        float currentX = getX() + PADDING;
        float currentY = getY() + Fonts.interMedium.get(15).getHeight() + 4;
        float rowHeight = 0;

        for (BoolValue boolValue : setting.getValues()) {
            float textWidth = Fonts.interRegular.get(13).getStringWidth(boolValue.getName());
            float optionWidth = textWidth + PADDING * 2;

            if (currentX + optionWidth > getX() + getWidth() - PADDING) {
                currentX = getX() + PADDING;
                currentY += rowHeight + PADDING / 2f;
                rowHeight = 0;
            }

            rowHeight = Math.max(rowHeight, OPTION_HEIGHT);

            boolean isSelected = boolValue.get();
            boolean isHovered = MouseUtils.isHovered2(currentX, currentY, optionWidth, OPTION_HEIGHT, mouseX, mouseY);

            Color baseColor = INSTANCE.getModuleManager().getModule(ClickGUI.class).color.get();
            Color bgColor = isSelected
                    ? baseColor
                    : ColorUtils.reAlpha(new Color(60, 60, 65), isHovered ? 200 : 150);

            RoundedUtils.drawRound(currentX, currentY, optionWidth, OPTION_HEIGHT, 4f, bgColor);

            Color textColor = isSelected ? Color.WHITE : new Color(200, 200, 200);
            Fonts.interRegular.get(13).drawString(boolValue.getName(), currentX + PADDING, currentY + OPTION_HEIGHT / 2f - 4, textColor.getRGB());

            currentX += optionWidth + PADDING;
        }

        setHeight(currentY + rowHeight - getY() + PADDING);
        super.drawScreen(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) return;

        float currentX = getX() + PADDING;
        float currentY = getY() + Fonts.interMedium.get(15).getHeight() + 4;
        float rowHeight = 0;

        for (BoolValue boolValue : setting.getValues()) {
            float textWidth = Fonts.interRegular.get(13).getStringWidth(boolValue.getName());
            float optionWidth = textWidth + PADDING * 2;

            if (currentX + optionWidth > getX() + getWidth() - PADDING) {
                currentX = getX() + PADDING;
                currentY += rowHeight + PADDING / 2f;
                rowHeight = 0;
            }

            rowHeight = Math.max(rowHeight, OPTION_HEIGHT);

            if (MouseUtils.isHovered2(currentX, currentY, optionWidth, OPTION_HEIGHT, mouseX, mouseY)) {
                boolValue.set(!boolValue.get());
                break;
            }
            currentX += optionWidth + PADDING;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean isVisible() {
        return setting.canDisplay();
    }
}