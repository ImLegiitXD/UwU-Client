
package wtf.uwu.gui.click.astolfo.component;

import lombok.Getter;
import lombok.Setter;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.values.Value;
import wtf.uwu.features.values.impl.*;
import wtf.uwu.gui.click.Component;
import wtf.uwu.gui.click.IComponent;
import wtf.uwu.gui.click.astolfo.component.impl.*;
import wtf.uwu.gui.font.Fonts;
import wtf.uwu.utils.render.MouseUtils;
import wtf.uwu.utils.render.RenderUtils;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class ModuleComponent implements IComponent {
    @Setter
    private float x, y, width, height;
    private final Module module;
    private final CopyOnWriteArrayList<Component> values = new CopyOnWriteArrayList<>();

    public ModuleComponent(Module module) {
        this.module = module;
        for (Value value : module.getValues()) {
            if (value instanceof BoolValue boolValue) {
                values.add(new BooleanComponent(boolValue));
            }
            if (value instanceof ColorValue colorValue) {
                values.add(new ColorComponent(colorValue));
            }
            if (value instanceof SliderValue sliderValue) {
                values.add(new SliderComponent(sliderValue));
            }
            if (value instanceof ModeValue modeValue) {
                values.add(new ModeComponent(modeValue));
            }
            if (value instanceof MultiBoolValue multiBoolValue) {
                values.add(new MultiBooleanComponent(multiBoolValue));
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {

        float yOffset = 11;
        if (!module.isExpanded()) {
            RenderUtils.drawRect(getX() + 3F, y, width - 5, 11F, new Color(36, 36, 36));
            if (module.isEnabled()) {
                RenderUtils.drawRect(getX() + 3F, y, width - 5, 11F, new Color(164, 53, 144));
            }
        }

        if (MouseUtils.isHovered2(x, y, width, 11F, mouseX, mouseY)) {
            if (!module.isExpanded() && !module.isEnabled()) {
                RenderUtils.drawRect(getX() + 3F, y, width - 5, 11F, new Color(255, 255, 255, 50));
            }
        }

        Fonts.interSemiBold.get(15).drawString(module.getName().toLowerCase(),
                getX() + width - Fonts.interSemiBold.get(15).getStringWidth(module.getName().toLowerCase()) - 3F,
                y + 4F,
                module.isEnabled() && module.isExpanded() ? new Color(164, 53, 144).getRGB()
                        : new Color(160, 160, 160).getRGB());

        if (module.isExpanded()) {
            for (Component component : values) {
                if (!component.isVisible()) continue;
                component.setX(x);
                component.setY(y + yOffset);
                component.setWidth(width);
                component.drawScreen(mouseX, mouseY);

                yOffset += component.getHeight();
            }
        }

        this.height = yOffset;

        IComponent.super.drawScreen(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        if (MouseUtils.isHovered2(x, y, width, 11F, mouseX, mouseY)) {
            if (mouseButton == 1) {
                if (!module.getValues().isEmpty())
                    module.setExpanded(!module.isExpanded());
            }

            if (mouseButton == 0) {
                module.toggle();
            }
        }

        for (Component value : values){
            value.mouseClicked(mouseX, mouseY, mouseButton);
        }

        IComponent.super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
