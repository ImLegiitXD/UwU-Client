package wtf.uwu.features.modules.impl.combat;

import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.packet.PacketEvent;
import wtf.uwu.events.impl.player.UpdateEvent;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.features.values.impl.SliderValue;
import wtf.uwu.utils.math.TimerUtils;

@ModuleInfo(name = "BlockHit", category = ModuleCategory.Legit)
public class BlockHit extends Module {

    private final SliderValue blockDelay = new SliderValue("Block Delay (ms)", 20, 0, 200, 1, this);

    private final TimerUtils blockTimer = new TimerUtils();
    private boolean shouldBlock;

    @Override
    public void onDisable() {
        unblock(); 
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        setTag("Reactive");

        if (!isHoldingSword()) {
            if (mc.thePlayer.isBlocking()) {
                unblock();
            }
            shouldBlock = false;
            return;
        }

        if (shouldBlock && blockTimer.hasTimeElapsed((long) blockDelay.get())) {
            mc.gameSettings.keyBindUseItem.setPressed(true);
            mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
            shouldBlock = false;
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getState() == PacketEvent.State.OUTGOING) {
            Packet<?> packet = event.getPacket();

            if (packet instanceof C02PacketUseEntity) {
                C02PacketUseEntity useEntityPacket = (C02PacketUseEntity) packet;
                if (useEntityPacket.getAction() == C02PacketUseEntity.Action.ATTACK && isHoldingSword()) {
                    shouldBlock = true;
                    blockTimer.reset();
                }
            }
        }
    }


    public void unblock() {
        if (mc.thePlayer.isBlocking()) {
            mc.gameSettings.keyBindUseItem.setPressed(false);
            mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
        }
    }

    private boolean isHoldingSword() {
        return mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword;
    }
}