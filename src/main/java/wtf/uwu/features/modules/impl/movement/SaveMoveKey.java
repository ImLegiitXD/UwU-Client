package wtf.uwu.features.modules.impl.movement;

import net.minecraft.client.settings.KeyBinding;
import org.lwjglx.input.Keyboard;
import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.misc.TickEvent;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;

@ModuleInfo(name = "SaveMoveKey",category = ModuleCategory.Movement)
public class SaveMoveKey extends Module {
    private boolean wasInventoryOpen = false;

    @EventTarget
    private void onTick(TickEvent event) {
        if (mc.currentScreen != null) {
            wasInventoryOpen = true;
        } else {
            if (wasInventoryOpen) {
                mc.addScheduledTask(() -> {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()));
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()));
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()));
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode()));
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindSprint.getKeyCode()));
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode()));
                });
                wasInventoryOpen = false;
            }
        }
    }
}
