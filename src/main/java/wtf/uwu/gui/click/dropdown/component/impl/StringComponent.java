package wtf.uwu.gui.click.dropdown.component.impl;

import net.minecraft.client.gui.GuiScreen;
import org.apache.commons.lang3.math.NumberUtils;
import org.lwjglx.input.Keyboard;
import wtf.uwu.features.modules.impl.visual.ClickGUI;
import wtf.uwu.features.values.impl.TextValue;
import wtf.uwu.gui.click.Component;
import wtf.uwu.gui.font.Fonts;
import wtf.uwu.utils.animations.Animation;
import wtf.uwu.utils.animations.Direction;
import wtf.uwu.utils.animations.impl.DecelerateAnimation;
import wtf.uwu.utils.render.ColorUtils;
import wtf.uwu.utils.render.MouseUtils;
import wtf.uwu.utils.render.RoundedUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StringComponent extends Component {
    private final TextValue setting;
    private final Animation input = new DecelerateAnimation(250, 1);
    private boolean inputting;
    private String text = "";

    public StringComponent(TextValue setting) {
        this.setting = setting;
        setHeight(Fonts.interRegular.get(14).getHeight() * 2 + 4);
        input.setDirection(Direction.BACKWARDS);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        input.setDirection(inputting ? Direction.FORWARDS : Direction.BACKWARDS);
        text = setting.get();
        if (setting.isOnlyNumber() && !NumberUtils.isNumber(text)) {
            text = text.replaceAll("[a-zA-Z]", "");
            setting.setText(text);
        }

        String textToDraw = text.isEmpty() && !inputting ? "Empty..." : text;
        if (inputting && System.currentTimeMillis() % 1000 > 500) {
            textToDraw += "|";
        }

        Fonts.interRegular.get(14).drawString(setting.getName(), getX() + 4, getY(), -1);

        float inputY = getY() + Fonts.interRegular.get(14).getHeight();

        Color borderColor = inputting ? INSTANCE.getModuleManager().getModule(ClickGUI.class).color.get() : new Color(70, 70, 75);
        RoundedUtils.drawRound(getX() + 2, inputY - 2, getWidth() - 4, Fonts.interRegular.get(14).getHeight() + 6, 2, new Color(40, 40, 45));
        RoundedUtils.drawRoundOutline(getX() + 2, inputY - 2, getWidth() - 4, Fonts.interRegular.get(14).getHeight() + 6, 2, 1, borderColor, borderColor);

        Color textColor = (text.isEmpty() && !inputting) ? Color.GRAY : ColorUtils.interpolateColorC(Color.GRAY, Color.WHITE, (float) input.getOutput());
        drawTextWithLineBreaks(textToDraw, getX() + 6, inputY + 2, getWidth() - 12, textColor);

        super.drawScreen(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        float inputY = getY() + Fonts.interRegular.get(14).getHeight();
        if (MouseUtils.isHovered2(getX() + 2, inputY - 2, getWidth() - 4, Fonts.interRegular.get(14).getHeight() + 6, mouseX, mouseY) && mouseButton == 0) {
            inputting = !inputting;
        } else {
            inputting = false;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (inputting) {
            if (setting.isOnlyNumber() && !Character.isDigit(typedChar) && keyCode != Keyboard.KEY_BACK) {
                return;
            }

            if (keyCode == Keyboard.KEY_BACK) {
                deleteLastCharacter();
            } else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
                text += GuiScreen.getClipboardString();
                setting.setText(text);
            } else if (text.length() < 59 && (Character.isLetterOrDigit(typedChar) || Character.isWhitespace(typedChar) || ".,!?@#$%^&*()_+-=[]{};:'\"<>/?\\|".indexOf(typedChar) != -1)) {
                text += typedChar;
                setting.setText(text);
            }
        }
        super.keyTyped(typedChar, keyCode);
    }

    private void drawTextWithLineBreaks(String text, float x, float y, float maxWidth, Color color) {
        List<String> wrappedLines = wrapText(text, maxWidth);
        float currentY = y;
        for (String wrappedLine : wrappedLines) {
            Fonts.interRegular.get(14).drawString(wrappedLine, x, currentY, color.getRGB());
            currentY += Fonts.interRegular.get(14).getHeight();
        }
        setHeight(currentY - y + Fonts.interRegular.get(14).getHeight() + 4);
    }

    private List<String> wrapText(String text, float maxWidth) {
        List<String> lines = new ArrayList<>();
        if (Fonts.interRegular.get(14).getStringWidth(text) <= maxWidth) {
            lines.add(text);
            return lines;
        }

        StringBuilder currentLine = new StringBuilder();
        String[] words = text.split(" ");
        for (String word : words) {
            if (Fonts.interRegular.get(14).getStringWidth(currentLine.toString() + word) > maxWidth) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder();
            }
            currentLine.append(word).append(" ");
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString().trim());
        }
        return lines;
    }

    private void deleteLastCharacter() {
        if (!text.isEmpty()) {
            text = text.substring(0, text.length() - 1);
            setting.setText(text);
        }
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }
}