package wtf.uwu.features.modules.impl.movement;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C0BPacketEntityAction.Action;
import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.packet.PacketEvent;
import wtf.uwu.events.impl.player.UpdateEvent;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.features.values.impl.BoolValue;
import wtf.uwu.utils.player.MovementCorrection;
import wtf.uwu.utils.player.MovementUtils;
import wtf.uwu.utils.player.RotationUtils;

@ModuleInfo(name = "Sprint", category = ModuleCategory.Movement)
public class Sprint extends Module {

    private final BoolValue omni = new BoolValue("Omni", false, this);
    private final BoolValue silent = new BoolValue("Silent", false, this);
    private final BoolValue rotate = new BoolValue("Rotate", false, this);
    private final BoolValue onlyOnGround = new BoolValue("Only On Ground", true, this, rotate::get);
    private final BoolValue noPackets = new BoolValue("No Packets", false, this);

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (!isEnabled(Scaffold.class)) {
            if (!noPackets.get()) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
            }
        }

        if (omni.get()) {
            mc.thePlayer.omniSprint = MovementUtils.isMoving();
        }

        if (silent.get()) {
            mc.thePlayer.serverSprintState = false;
        }

        if (rotate.get()) {
            if (onlyOnGround.get() && !mc.thePlayer.onGround) return;

            float[] finalRotation = new float[]{
                    MovementUtils.getRawDirection(),
                    mc.thePlayer.rotationPitch
            };

            RotationUtils.setRotation(finalRotation, MovementCorrection.SILENT);
        }

        if (noPackets.get()) {
            mc.thePlayer.setSprinting(true);
            mc.thePlayer.serverSprintState = false;
        }
    }

    @EventTarget
    public void onPacketSend(PacketEvent event) {
        if (!noPackets.get()) return;

        if (event. getPacket() instanceof C0BPacketEntityAction packet) {
            if (packet.getAction() == Action.START_SPRINTING || packet.getAction() == Action.STOP_SPRINTING) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void onDisable() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
        mc.thePlayer.omniSprint = false;
    }
}

