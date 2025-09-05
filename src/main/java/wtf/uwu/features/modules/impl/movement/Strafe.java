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
package wtf.uwu.features.modules.impl.movement;

import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.player.UpdateEvent;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.features.values.impl.BoolValue;
import wtf.uwu.utils.player.MovementUtils;

@ModuleInfo(name = "Strafe", category = ModuleCategory.Movement)
public class Strafe extends Module {

    public final BoolValue ground = new BoolValue("Ground", true, this);
    public final BoolValue air = new BoolValue("Air", true, this);

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mc.thePlayer.onGround && ground.get()) MovementUtils.strafe();
        if (!mc.thePlayer.onGround && air.get()) MovementUtils.strafe();
    }
}