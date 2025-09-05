package wtf.uwu.features.modules.impl.movement;

import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.player.MoveInputEvent;
import wtf.uwu.events.impl.player.UpdateEvent;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.features.values.impl.BoolValue;
import wtf.uwu.utils.player.MovementUtils;

@ModuleInfo(name = "InvMove", category = ModuleCategory.Movement)
public class InvMove extends Module {
    private final BoolValue cancelInventory = new BoolValue("NoInv", false, this);
    private final BoolValue cancelChest = new BoolValue("No Chest", false, this);
    private final KeyBinding[] keyBindings = new KeyBinding[]{mc.gameSettings.keyBindForward, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindJump};

    @Override
    public void onDisable() {
        for (KeyBinding keyBinding : this.keyBindings) {
            KeyBinding.setKeyBindState(keyBinding.getKeyCode(), false);
        }
    }


    @EventTarget
    private void onUpdate(UpdateEvent event) {
        if (!(mc.currentScreen instanceof GuiChat) && !(mc.currentScreen instanceof GuiIngameMenu)) {
            if (cancelInventory.get() && (mc.currentScreen instanceof GuiContainer))
                return;

            if (cancelChest.get() && mc.currentScreen instanceof GuiChest)
                return;

            for (KeyBinding keyBinding : this.keyBindings) {
                KeyBinding.setKeyBindState(keyBinding.getKeyCode(), GameSettings.isKeyDown(keyBinding));
            }


        }
    }
}
