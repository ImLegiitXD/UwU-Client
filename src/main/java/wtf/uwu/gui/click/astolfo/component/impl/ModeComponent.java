
package wtf.uwu.gui.click.astolfo.component.impl;

import wtf.uwu.features.values.impl.ModeValue;
import wtf.uwu.gui.click.Component;
import wtf.uwu.gui.font.Fonts;
import wtf.uwu.utils.render.MouseUtils;

import java.util.Objects;

public class ModeComponent extends Component {

    private final ModeValue value;

    public ModeComponent(ModeValue value) {
        this.value = value;
        setHeight(11);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        Fonts.interSemiBold.get(13).drawString(value.getName(), getX() + 5F, getY() + 4F, -1);
        Fonts.interSemiBold.get(13).drawString(value.get(),
                getX() + (getWidth() - 5) - Fonts.interSemiBold.get(13).getStringWidth(value.get()), getY() + 4F,
                -1);
        super.drawScreen(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (MouseUtils.isHovered2(getX(), getY(), 100F, getHeight(), mouseX, mouseY)) {
            if (mouseButton == 0) {
                boolean neverBreak = true;
                for (int index = 0; index < value.getModes().length; index++) {
                    if (Objects.equals(value.get(), value.getModes()[index])) {
                        if (index == value.getModes().length - 1) {
                            value.set(value.getModes()[0]);
                            neverBreak = false;
                            break;
                        } else {
                            value.set(value.getModes()[index + 1]);
                            neverBreak = false;
                            break;
                        }
                    }
                }

                if (neverBreak) {
                    value.set(value.getModes()[0]);
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean isVisible() {
        return this.value.canDisplay();
    }
}
