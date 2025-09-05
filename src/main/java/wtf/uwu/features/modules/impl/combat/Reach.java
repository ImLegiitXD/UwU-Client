package wtf.uwu.features.modules.impl.combat;

import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.misc.MouseOverEvent;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.features.values.impl.SliderValue;
import wtf.uwu.utils.math.MathUtils;

@ModuleInfo(name = "Reach", category = ModuleCategory.Combat)
public class Reach extends Module {

    public final SliderValue min = new SliderValue("Min Range", 3.0F, 3, 6F, .1f, this);
    public final SliderValue max = new SliderValue("Max Range", 3.3F, 3, 6F, .1f, this);

    @EventTarget
    public void onMouseOver(MouseOverEvent event) {
        event.setRange(MathUtils.randomizeDouble(min.getMin(), max.getMax()));
    }
}