/*
 * MoonLight Hacked Client
 *
 * A free and open-source hacked client for Minecraft.
 * Developed using Minecraft's resources.
 *
 * Repository: https://github.com/randomguy3725/MoonLight
 *
 * Author(s): [Randumbguy & wxdbie & opZywl & MukjepScarlet & lucas & eonian]
 */
package wtf.uwu.features.modules.impl.player;

import net.minecraft.item.ItemBlock;
import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.player.MotionEvent;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.features.values.impl.SliderValue;
import wtf.uwu.utils.math.MathUtils;
import wtf.uwu.utils.player.PlayerUtils;

@ModuleInfo(name = "FastPlace", category = ModuleCategory.Player)
public class FastPlace extends Module {

    public final SliderValue speed = new SliderValue("Speed", 1, 0, 4, this);

    @EventTarget
    public void onMotion(MotionEvent event) {
        setTag(MathUtils.incValue(speed.get(), 1) + "");
        if (!PlayerUtils.nullCheck())
            return;
        if (mc.thePlayer.getHeldItem() == null)
            return;
        if (mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock)
            mc.rightClickDelayTimer = (int) speed.get();
    }
}
