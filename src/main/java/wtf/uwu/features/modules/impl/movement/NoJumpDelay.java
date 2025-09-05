package wtf.uwu.features.modules.impl.movement;

import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.player.UpdateEvent;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;

@ModuleInfo(name = "NoJumpDelay", category = ModuleCategory.Movement)
public class NoJumpDelay extends Module {

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        mc.thePlayer.jumpTicks = 0;
    }
}
