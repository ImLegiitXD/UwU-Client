package wtf.uwu.features.modules.impl.combat;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.player.AttackEvent;
import wtf.uwu.events.impl.player.UpdateEvent;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.features.values.impl.BoolValue;
import wtf.uwu.features.values.impl.ModeValue;
import wtf.uwu.utils.player.MovementUtils;

import static wtf.uwu.utils.InstanceAccess.mc;

@ModuleInfo(name = "MoreKB", category = ModuleCategory.Legit)
public class MoreKB extends Module {

    private final ModeValue mode = new ModeValue("Mode", new String[]{"Legit Fast", "Packet", "Lesspaket", "Polar"}, "Packet", this);
    private final BoolValue onlyGround = new BoolValue("Only Ground", true, this);

    private int ticks;
    private EntityLivingBase target;

    @EventTarget
    public void onAttack(AttackEvent event) {
        if (mc.thePlayer.isSprinting() && event.getTargetEntity() instanceof EntityLivingBase) {
            this.target = (EntityLivingBase) event.getTargetEntity();
            this.ticks = 2;
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (target == null) return;

        boolean canTap = MovementUtils.isMoving() && (!onlyGround.get() || mc.thePlayer.onGround);
        String currentMode = mode.get();

        if (currentMode.equalsIgnoreCase("Polar")) {
            if (ticks == 2 && canTap) {
                mc.thePlayer.setSprinting(false);
            } else if (ticks == 1) {
                mc.thePlayer.setSprinting(true);
                reset();
            }
        } else {
            if (ticks == 1 && canTap) {
                switch (currentMode) {
                    case "Legit Fast":
                        mc.thePlayer.sprintingTicksLeft = 0;
                        break;
                    case "Packet":
                        sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                        sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                        break;
                    case "Lesspaket":
                        mc.thePlayer.setSprinting(false);
                        mc.thePlayer.setSprinting(true);
                        break;
                }
                reset();
            } else if (ticks < 1) {
                reset();
            }
        }

        if (ticks > 0) {
            ticks--;
        }
    }

    private void reset() {
        this.target = null;
        this.ticks = 0;
    }
}
