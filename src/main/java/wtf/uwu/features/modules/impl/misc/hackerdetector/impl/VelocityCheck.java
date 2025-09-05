package wtf.uwu.features.modules.impl.misc.hackerdetector.impl;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S14PacketEntity;
import wtf.uwu.events.impl.packet.PacketEvent;
import wtf.uwu.features.modules.impl.misc.hackerdetector.Check;
import wtf.uwu.utils.player.MovementUtils;

public class VelocityCheck extends Check {

    @Override
    public String getName() {
        return "Velocity";
    }

    @Override
    public void onPacketReceive(PacketEvent event, EntityPlayer player) {

        if (event.getPacket() instanceof S14PacketEntity) {
            if (MovementUtils.getSpeed(player) == 0.0 && player.hurtResistantTime > 6 && player.hurtResistantTime < 1 && !mc.theWorld.checkBlockCollision(player.getEntityBoundingBox().expand(0.05, 0.0, 0.05))) {
                flag(player, "Invalid velocity");
            }
        }
    }

    @Override
    public void onUpdate(EntityPlayer player) {

    }
}

