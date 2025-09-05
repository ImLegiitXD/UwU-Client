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
package wtf.uwu.features.modules.impl.misc.hackerdetector.impl;

import net.minecraft.entity.player.EntityPlayer;
import wtf.uwu.events.impl.packet.PacketEvent;
import wtf.uwu.features.modules.impl.misc.hackerdetector.Check;

public class NoFallCheck extends Check {
    boolean fall;

    @Override
    public String getName() {
        return "No Fall";
    }

    @Override
    public void onPacketReceive(PacketEvent event, EntityPlayer player) {

    }

    @Override
    public void onUpdate(EntityPlayer player) {
        if (player.fallDistance > 3) {
            fall = true;
        }
        if (fall && player.fallDistance == 0 && player.hurtTime == 0 && !player.isInWater()) {
            flag(player, "Not taking any damage");
            fall = false;
        }
    }


}

