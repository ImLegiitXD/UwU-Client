package wtf.uwu.features.modules.impl.visual;

import net.minecraft.client.gui.GuiScreen;
import org.lwjglx.input.Keyboard;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.features.values.impl.BoolValue;
import wtf.uwu.features.values.impl.ColorValue;
import wtf.uwu.features.values.impl.ModeValue;

import java.awt.*;

@ModuleInfo(name = "ClickGUI", category = ModuleCategory.Visual, key = Keyboard.KEY_RSHIFT)
public class ClickGUI extends Module {
    public final ModeValue mode = new ModeValue("Mode", new String[]{"Augustus", "NeverLose", "DropDown", "Astolfo"}, "DropDown", this);

    public final ColorValue color = new ColorValue("Color", new Color(128, 128, 255), this);
    public final BoolValue rainbow = new BoolValue("Rainbow",true,this,() -> mode.is("Augustus"));

    @Override
    public void onEnable() {
        GuiScreen guiScreen = switch (mode.get()) {
            case "NeverLose" -> INSTANCE.getNeverLose();
            case "DropDown" -> INSTANCE.getDropdownGUI();
            case "Augustus" -> INSTANCE.getAstolfoGui();
            case "Astolfo" -> INSTANCE.getAstolfoGui();
            default -> null;
        };
        mc.displayGuiScreen(guiScreen);
        toggle();
        super.onEnable();
    }
}