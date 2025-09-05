package wtf.uwu.gui.click.dropdown.panel;

import kotlin.collections.CollectionsKt;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import wtf.uwu.UwU;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.impl.visual.ClickGUI;
import wtf.uwu.gui.click.IComponent;
import wtf.uwu.gui.click.dropdown.component.ModuleComponent;
import wtf.uwu.gui.font.Fonts;
import wtf.uwu.utils.animations.Direction;
import wtf.uwu.utils.animations.impl.EaseInOutQuad;
import wtf.uwu.utils.render.ColorUtils;
import wtf.uwu.utils.render.MouseUtils;
import wtf.uwu.utils.render.RenderUtils;
import wtf.uwu.utils.render.RoundedUtils;
import java.awt.*;
import java.util.List;

@Getter
@Setter
public class CategoryPanel implements IComponent {
    private static final float DEFAULT_WIDTH = 125f;
    private static final float BORDER_RADIUS = 12f;
    private static final float HEADER_HEIGHT = 24f;
    private static final float COMPONENT_PADDING = 8f;
    private static final int ANIMATION_DURATION = 300;
    private static final float ANIMATION_THRESHOLD = 0.6f;

    private float x, y, dragX, dragY;
    private float width = DEFAULT_WIDTH;
    private float height;
    private boolean dragging, opened;

    private final EaseInOutQuad openAnimation = new EaseInOutQuad(ANIMATION_DURATION, 1);
    private final EaseInOutQuad hoverAnimation = new EaseInOutQuad(200, 1);
    private final ModuleCategory category;
    private final List<ModuleComponent> moduleComponents;

    private ScaledResolution scaledResolution;
    private Color mainColor;
    private boolean needsColorUpdate = true;

    public CategoryPanel(ModuleCategory category) {
        this.category = category;
        this.openAnimation.setDirection(Direction.BACKWARDS);
        this.hoverAnimation.setDirection(Direction.BACKWARDS);

        this.moduleComponents = CollectionsKt.map(
                UwU.INSTANCE.getModuleManager().getModulesByCategory(category),
                ModuleComponent::new
        );
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        update(mouseX, mouseY);

        if (scaledResolution == null) {
            scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        }

        float scaleX = scaledResolution.getScaledWidth() / 2f;
        float scaleY = scaledResolution.getScaledHeight() / 2f;

        RenderUtils.scaleStart(scaleX, scaleY, (float) UwU.INSTANCE.getDropdownGUI().getOpeningAnimation().getOutput());

        renderBackground(mouseX, mouseY);
        renderHeader(mouseX, mouseY);
        renderComponents(mouseX, mouseY);

        RenderUtils.scaleEnd();
        IComponent.super.drawScreen(mouseX, mouseY);
    }

    private void renderBackground(int mouseX, int mouseY) {
        updateMainColor();

        float panelHeight = (float) (HEADER_HEIGHT + ((height - HEADER_HEIGHT) * openAnimation.getOutput()));
        boolean isHovered = isHeaderHovered(mouseX, mouseY);
        hoverAnimation.setDirection(isHovered ? Direction.FORWARDS : Direction.BACKWARDS);
        Color baseColor = ColorUtils.interpolateColorC(mainColor, Color.WHITE, 0.1f);
        Color topColor = ColorUtils.reAlpha(baseColor, (int)(200 + 20 * hoverAnimation.getOutput()));
        Color bottomColor = ColorUtils.reAlpha(ColorUtils.darker(baseColor, 0.7f), (int)(180 + 25 * hoverAnimation.getOutput()));

        RoundedUtils.drawGradientRound(
                x - 1, y - 3, width + 2, panelHeight + 4,
                BORDER_RADIUS + 1,
                new Color(0, 0, 0, (int)(40 + 15 * hoverAnimation.getOutput())),
                new Color(0, 0, 0, (int)(20 + 10 * hoverAnimation.getOutput())),
                new Color(0, 0, 0, (int)(40 + 15 * hoverAnimation.getOutput())),
                new Color(0, 0, 0, (int)(20 + 10 * hoverAnimation.getOutput()))
        );

        RoundedUtils.drawGradientRound(
                x, y, width, panelHeight,
                BORDER_RADIUS,
                bottomColor, // bottom-left
                topColor,    // top-left
                bottomColor, // bottom-right
                topColor     // top-right
        );

        Color borderColor = ColorUtils.reAlpha(ColorUtils.brighter(mainColor, 0.3f),
                (int)(80 + 40 * hoverAnimation.getOutput()));
        RoundedUtils.drawRoundOutline(x, y, width, panelHeight, BORDER_RADIUS, 1.5f, borderColor, borderColor);

        if (opened) {

            RenderUtils.drawGradientRect(
                    x + 6, y + HEADER_HEIGHT - 0.5f,
                    x + width - 6, y + HEADER_HEIGHT + 0.5f,
                    false,
                    ColorUtils.reAlpha(mainColor, 20).getRGB(),
                    ColorUtils.reAlpha(mainColor, 80).getRGB()
            );
        }
    }

    private void renderHeader(int mouseX, int mouseY) {
        updateMainColor();
        Color textColor = ColorUtils.interpolateColorC(Color.WHITE, mainColor, 0.2f);
        Color iconColor = ColorUtils.interpolateColorC(mainColor, Color.WHITE, 0.3f);

        float textY = y + HEADER_HEIGHT / 2 - 4f;
        float iconSize = 16f;
        float iconScale = (float)(1.0f + 0.1f * hoverAnimation.getOutput());
        RenderUtils.scaleStart(x + 8, textY + 4, iconScale);
        String categoryIcon = getCategoryIcon(category);
        Fonts.interSemiBold.get(iconSize).drawString(categoryIcon, x + 8, textY, iconColor.getRGB());

        RenderUtils.scaleEnd();
        Fonts.interSemiBold.get(16).drawString(category.getName(), x + 28, textY, textColor.getRGB());
        renderToggleIndicator(mouseX, mouseY);
    }

    private void renderToggleIndicator(int mouseX, int mouseY) {
        float indicatorX = x + width - 20;
        float indicatorY = y + HEADER_HEIGHT / 2 - 3;
        float rotation = (float)(opened ? 180 * openAnimation.getOutput() : 0);

        RenderUtils.pushMatrix();
        RenderUtils.translate(indicatorX + 6, indicatorY + 3, 0);
        RenderUtils.rotate(rotation, 0, 0, 1);

        Color indicatorColor = ColorUtils.interpolateColorC(Color.LIGHT_GRAY, mainColor, 0.5f);
        Fonts.interRegular.get(12).drawCenteredString("▼", 0, -3, indicatorColor.getRGB());

        RenderUtils.popMatrix();
    }

    private void renderComponents(int mouseX, int mouseY) {
        float componentOffsetY = HEADER_HEIGHT + 6;
        boolean shouldRenderComponents = openAnimation.getOutput() > ANIMATION_THRESHOLD;

        for (ModuleComponent component : moduleComponents) {
            component.setX(x + COMPONENT_PADDING);
            component.setY(y + componentOffsetY);
            component.setWidth(width - COMPONENT_PADDING * 2);

            if (shouldRenderComponents) {
                float alpha = (float)Math.min(1.0, (openAnimation.getOutput() - ANIMATION_THRESHOLD) / (1.0 - ANIMATION_THRESHOLD));
                RenderUtils.setAlphaLimit(alpha);
                component.drawScreen(mouseX, mouseY);
                RenderUtils.setAlphaLimit(1.0f);
            }

            componentOffsetY += (float) (component.getHeight() * openAnimation.getOutput());
        }

        height = componentOffsetY + 8;
    }

    private String getCategoryIcon(ModuleCategory category) {
        if (category == ModuleCategory.Combat) {
            return "";
        } else if (category == ModuleCategory.Legit) {
            return "";
        } else if (category == ModuleCategory.Movement) {
            return "";
        } else if (category == ModuleCategory.Player) {
            return "";
        } else if (category == ModuleCategory.Misc) {
            return "";
        } else if (category == ModuleCategory.Exploit) {
            return "";
        } else if (category == ModuleCategory.Visual) {
            return "";
        } else {
            return "●";
        }
    }

    private void updateMainColor() {
        if (needsColorUpdate) {
            mainColor = UwU.INSTANCE.getModuleManager().getModule(ClickGUI.class).color.get();
            needsColorUpdate = false;
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHeaderHovered(mouseX, mouseY)) {
            handleHeaderClick(mouseX, mouseY, mouseButton);
        } else if (opened) {
            handleComponentClicks(mouseX, mouseY, mouseButton);
        }
        IComponent.super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private boolean isHeaderHovered(int mouseX, int mouseY) {
        return MouseUtils.isHovered2(x, y, width, HEADER_HEIGHT, mouseX, mouseY);
    }

    private void handleHeaderClick(int mouseX, int mouseY, int mouseButton) {
        switch (mouseButton) {
            case 0:
                startDragging(mouseX, mouseY);
                break;
            case 1:
                togglePanel();
                break;
        }
    }

    private void startDragging(int mouseX, int mouseY) {
        dragging = true;
        dragX = x - mouseX;
        dragY = y - mouseY;
    }

    private void togglePanel() {
        opened = !opened;
        needsColorUpdate = true;
    }

    private void handleComponentClicks(int mouseX, int mouseY, int mouseButton) {
        moduleComponents.forEach(component ->
                component.mouseClicked(mouseX, mouseY, mouseButton));
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        moduleComponents.forEach(component -> component.keyTyped(typedChar, keyCode));
        IComponent.super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == 0) {
            dragging = false;
        }
        moduleComponents.forEach(component ->
                component.mouseReleased(mouseX, mouseY, state));
        IComponent.super.mouseReleased(mouseX, mouseY, state);
    }

    private void update(int mouseX, int mouseY) {
        updateAnimation();
        updatePosition(mouseX, mouseY);
    }

    private void updateAnimation() {
        this.openAnimation.setDirection(opened ? Direction.FORWARDS : Direction.BACKWARDS);
    }

    private void updatePosition(int mouseX, int mouseY) {
        if (dragging) {
            x = mouseX + dragX;
            y = mouseY + dragY;
        }
    }

    public void invalidateCache() {
        scaledResolution = null;
        needsColorUpdate = true;
    }
}