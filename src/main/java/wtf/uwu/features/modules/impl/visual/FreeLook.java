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
package wtf.uwu.features.modules.impl.visual;

import org.lwjglx.input.Mouse;
import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.player.MotionEvent;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;

@ModuleInfo(name = "FreeLook", category = ModuleCategory.Visual)
public class FreeLook extends Module {
    private boolean released;

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (event.isPost()) {
            if (Mouse.isButtonDown(2)) {
                mc.gameSettings.thirdPersonView = 1;
                released = false;
            } else {
                if (!released) {
                    mc.gameSettings.thirdPersonView = 0;
                    released = true;
                }
            }
        }
    }
}
