package wtf.uwu.gui.click.dropdown;

import lombok.Getter;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import wtf.uwu.UwU;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.impl.visual.ClickGUI;
import wtf.uwu.gui.click.dropdown.panel.CategoryPanel;
import wtf.uwu.gui.font.Fonts;
import wtf.uwu.utils.animations.Animation;
import wtf.uwu.utils.animations.Direction;
import wtf.uwu.utils.animations.impl.EaseInOutQuad;
import wtf.uwu.utils.animations.impl.EaseOutSine;
import wtf.uwu.utils.render.ColorUtils;
import wtf.uwu.utils.render.RenderUtils;
import wtf.uwu.utils.render.RoundedUtils;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DropdownGUI extends GuiScreen {
    @Getter
    private final EaseInOutQuad openingAnimation = new EaseInOutQuad(450, 1);
    private final EaseOutSine backgroundAnimation = new EaseOutSine(600, 1);

    private boolean closing;
    private final List<CategoryPanel> panels = new ArrayList<>();
    public int scroll;

    private static final float PANEL_SPACING = 15f;
    private static final float INITIAL_X = 60f;
    private static final float INITIAL_Y = 40f;
    private static final int BLUR_STRENGTH = 8;

    public DropdownGUI() {
        openingAnimation.setDirection(Direction.BACKWARDS);
        backgroundAnimation.setDirection(Direction.BACKWARDS);

        initializePanels();
    }

    private void initializePanels() {
        float currentX = INITIAL_X;

        for (ModuleCategory category : ModuleCategory.values()) {
            if (category == ModuleCategory.Search || category == ModuleCategory.Config) {
                continue;
            }

            CategoryPanel panel = new CategoryPanel(category);
            panels.add(panel);

            panel.setX(currentX);
            panel.setY(INITIAL_Y);

            currentX += panel.getWidth() + PANEL_SPACING;
        }
    }

    @Override
    public void initGui() {
        closing = false;
        openingAnimation.setDirection(Direction.FORWARDS);
        backgroundAnimation.setDirection(Direction.FORWARDS);
        panels.forEach(CategoryPanel::invalidateCache);
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        handleScrolling();

        int scrolledMouseY = mouseY + scroll;
        GlStateManager.translate(0, -scroll, 0);

        renderBackground();

        if (closing) {
            handleClosing();
        }

        renderHeader();
        renderPanels(mouseX, scrolledMouseY);

        GlStateManager.translate(0, scroll, 0);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void handleScrolling() {
        int wheel = Mouse.getDWheel();
        if (wheel != 0) {
            if (wheel > 0) {
                scroll -= 15;
            } else {
                scroll += 15;
            }
        }
    }

    private void renderBackground() {
        float alpha = (float) backgroundAnimation.getOutput();
        RenderUtils.drawRect(0, 0, this.width, this.height, new Color(0, 0, 0, (int) (100 * alpha)).getRGB());
    }

    private void handleClosing() {
        openingAnimation.setDirection(Direction.BACKWARDS);
        if (openingAnimation.isDone()) {
            mc.displayGuiScreen(null);
        }
    }

    private void renderHeader() {
        Fonts.interSemiBold.get(24).drawStringWithShadow(UwU.INSTANCE.clientName + " v" + UwU.INSTANCE.version, 15, 15, -1);
    }

    private void renderPanels(int mouseX, int mouseY) {
        for (CategoryPanel panel : panels) {
            panel.drawScreen(mouseX, mouseY);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        int scrolledMouseY = mouseY + scroll;
        for (CategoryPanel panel : panels) {
            panel.mouseClicked(mouseX, scrolledMouseY, mouseButton);
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        int scrolledMouseY = mouseY + scroll;
        for (CategoryPanel panel : panels) {
            panel.mouseReleased(mouseX, scrolledMouseY, state);
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            closing = true;
            return;
        }

        for (CategoryPanel panel : panels) {
            panel.keyTyped(typedChar, keyCode);
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        ClickGUI clickGUIModule = UwU.INSTANCE.getModuleManager().getModule(ClickGUI.class);
        if (clickGUIModule.isEnabled()) {
            clickGUIModule.toggle();
        }
        super.onGuiClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}