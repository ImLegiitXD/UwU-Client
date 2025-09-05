package wtf.uwu.features.modules.impl.visual;

import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.features.values.impl.BoolValue;
import wtf.uwu.features.values.impl.ColorValue;

import java.awt.*;

@ModuleInfo(name = "TEST", category = ModuleCategory.Visual)
public class EnchantGlint extends Module {

    public final BoolValue syncColor = new BoolValue("Sync Color", false, this);
    public final ColorValue color = new ColorValue("Color",new Color(0,255,255),this ,() -> !syncColor.get());
}