

package wtf.uwu.gui.click.astolfo.component.impl;

import wtf.uwu.features.values.impl.BoolValue;
import wtf.uwu.features.values.impl.MultiBoolValue;
import wtf.uwu.gui.click.Component;
import wtf.uwu.gui.font.Fonts;
import wtf.uwu.utils.render.MouseUtils;
import wtf.uwu.utils.render.RenderUtils;

import java.awt.*;

public class MultiBooleanComponent extends Component {

    private final MultiBoolValue value;
    private boolean expanded;

    public MultiBooleanComponent(MultiBoolValue value) {
        this.value = value;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        float offset = 0;
        if (expanded) {
            RenderUtils.drawRect(getX() + 3F, getY(), (getWidth() - 5), getHeight(), new Color(17, 17, 17));
        }
        Fonts.interSemiBold.get(13).drawCenteredString(value.getName() + "...",getX() +  50F,
                getY() + 4F, -1);

        if (expanded) {
            RenderUtils.drawRect(getX() + 3F, getY(), (getWidth() - 5),
                    offset, new Color(17, 17, 17));
            for (BoolValue boolValue : value.getValues()) {
                offset += 11;
                if (boolValue.get()) {
                    RenderUtils.drawRect(getX() + 3F, getY() + offset, (getWidth() - 5), 11,
                            new Color(164, 53, 144));
                }
                Fonts.interSemiBold.get(13).drawString(boolValue.getName(),getX() +  5F, getY() + 4F + offset, -1);
            }
        }
        setHeight(offset + 11);
        super.drawScreen(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        float offset = 0;
        if (MouseUtils.isHovered2(getX(), getY(), getWidth(), 11, mouseX, mouseY)) {
            if (mouseButton == 0) {
                expanded = !expanded;
            }
        }
        if (expanded) {
            for (BoolValue boolValue : value.getValues()) {
                offset += 11;
                if (MouseUtils.isHovered2(getX(), getY() + offset, 100F, 11, mouseX, mouseY)) {
                    if (mouseButton == 0) {
                        boolValue.set(!boolValue.get());
                    }
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
