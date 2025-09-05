

package wtf.uwu.gui.click.astolfo.component.impl;

import org.lwjglx.input.Mouse;
import wtf.uwu.features.values.impl.SliderValue;
import wtf.uwu.gui.click.Component;
import wtf.uwu.gui.font.Fonts;
import wtf.uwu.utils.math.MathUtils;
import wtf.uwu.utils.render.MouseUtils;
import wtf.uwu.utils.render.RenderUtils;

import java.awt.*;
import java.text.DecimalFormat;

public class SliderComponent extends Component {

    private final SliderValue value;
    private final DecimalFormat FLOAT_POINT_FORMAT = new DecimalFormat("0.00");

    public SliderComponent(SliderValue value) {
        this.value = value;
        setHeight(11);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        RenderUtils.drawRect(getX() + 3F, getY(),
                (getWidth() - 5) * (value.get() / value.getMax()), getHeight(),
                new Color(164, 53, 144));

        Fonts.interSemiBold.get(13).drawString(value.getName(),getX() +  5F, getY() + 4F, -1);
        Fonts.interSemiBold.get(13).drawCenteredString(FLOAT_POINT_FORMAT.format(value.get()),getX() +  88F,
                getY() + 4F, -1);
        if (MouseUtils.isHovered2(getX(), getY(),getX() +  (getWidth() - 5), getHeight(), mouseX, mouseY) && Mouse.isButtonDown(0)) {
            float set = Math.max(value.getMin(),
                    Float.parseFloat(
                            FLOAT_POINT_FORMAT.format(((((Math.max(0F, ((mouseX - getX()) / (getWidth() - 5)))
                                    * (value.getMax()))))))));
            value.setValue((float) MathUtils.incValue(set, value.getIncrement()));
        }

        super.drawScreen(mouseX, mouseY);
    }

    @Override
    public boolean isVisible() {
        return this.value.canDisplay();
    }
}
