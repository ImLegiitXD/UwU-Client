package wtf.uwu.features.modules.impl.combat;

import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.features.values.impl.ModeValue;

@ModuleInfo(name = "KeepSprint", category = ModuleCategory.Combat)
public class KeepSprint extends Module {

    public final ModeValue mode = new ModeValue("Mode", new String[]{"Vanilla"}, "Vanilla", this);
}
