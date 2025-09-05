package wtf.uwu.features.modules.impl.misc.hackerdetector;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;
import wtf.uwu.events.impl.packet.PacketEvent;
import wtf.uwu.features.modules.impl.misc.HackerDetector;
import wtf.uwu.gui.notification.NotificationType;
import wtf.uwu.utils.InstanceAccess;
import wtf.uwu.utils.math.TimerUtils;
import wtf.uwu.utils.misc.DebugUtils;

public abstract class Check implements InstanceAccess {

    public TimerUtils flagTimer = new TimerUtils();
    public int time = 2;

    public abstract String getName();

    public abstract void onPacketReceive(PacketEvent event, EntityPlayer player);

    public abstract void onUpdate(EntityPlayer player);

    public void onMotion(EntityPlayer player, double x, double y, double z) {
    }

    public void flag(EntityPlayer player, String verbose) {
        if (flagTimer.hasTimeElapsed(time * 1000L)) {
            DebugUtils.sendMessage(player.getName() + EnumChatFormatting.WHITE + " detected for " + EnumChatFormatting.GRAY + getName() + EnumChatFormatting.WHITE + ", " + EnumChatFormatting.WHITE + verbose);
            INSTANCE.getNotificationManager().post(NotificationType.WARNING, player.getName() + EnumChatFormatting.WHITE + " detected for " + EnumChatFormatting.GRAY + getName() + EnumChatFormatting.WHITE, verbose, 2);
            INSTANCE.getModuleManager().getModule(HackerDetector.class).mark(player);
            flagTimer.reset();
        }
    }
}