package wtf.uwu.features.modules.impl.misc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import wtf.uwu.UwU;
import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.packet.PacketEvent;
import wtf.uwu.events.impl.player.UpdateEvent;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.features.modules.impl.combat.AntiBot;
import wtf.uwu.features.modules.impl.misc.hackerdetector.Check;
import wtf.uwu.features.modules.impl.misc.hackerdetector.impl.*;
import wtf.uwu.features.values.impl.BoolValue;
import wtf.uwu.features.values.impl.MultiBoolValue;

import java.util.*;

@ModuleInfo(name = "HackerDetector", category = ModuleCategory.Misc)
public class HackerDetector extends Module {
    public final MultiBoolValue options = new MultiBoolValue("Detects", Arrays.asList(
            new BoolValue("Angle", false),
            new BoolValue("Auto Block", false),
            new BoolValue("Legit Scaffold", false),
            new BoolValue("Invalid motion", false),
            new BoolValue("No Fall", false),
            new BoolValue("No Slow", false),
            new BoolValue("Scaffold", false),
            new BoolValue("Velocity", false),
            new BoolValue("Omni Sprint", false)
    ), this);

    public final BoolValue selfCheck = new BoolValue("Self", false, this);
    private final Set<EntityPlayer> hackers = new HashSet<>();
    private final ArrayList<Check> checks = new ArrayList<>();

    public HackerDetector() {
        addChecks(
                new AngleCheck(),
                new AutoBlockCheck(),
                new LegitScaffoldCheck(),
                new MotionCheck(),
                new NoFallCheck(),
                new NoSlowCheck(),
                new ScaffoldCheck(),
                new VelocityCheck(),
                new OmniSprintCheck()

        );
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            for (Check check : checks) {
                if ((selfCheck.get() || player != mc.thePlayer) && !player.isDead && !UwU.INSTANCE.getFriendManager().isFriend(player)) {
                    if(isEnabled(AntiBot.class) && getModule(AntiBot.class).bots.contains(player))
                        continue;
                    if (options.isEnabled(check.getName())) {
                        check.onUpdate(player);
                    }
                }
            }
        }
    }

    @EventTarget
    public final void onPacketReceive(PacketEvent event) {
        if (event.getState() != PacketEvent.State.INCOMING) return;
        if (event.getPacket() instanceof S14PacketEntity || event.getPacket() instanceof S18PacketEntityTeleport) {
            for (EntityPlayer player : mc.theWorld.playerEntities) {
                for (Check check : checks) {
                    if ((selfCheck.get() || player != mc.thePlayer) && !player.isDead && !UwU.INSTANCE.getFriendManager().isFriend(player)) {
                        if(isEnabled(AntiBot.class) && getModule(AntiBot.class).isBot(player))
                            continue;
                        if (options.isEnabled(check.getName())) {
                            check.onPacketReceive(event, player);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        hackers.clear();
    }

    public void addChecks(Check... checks) {
        this.checks.addAll(Arrays.asList(checks));
    }

    public void mark(EntityPlayer ent) {
        hackers.add(ent);
    }

    public boolean isHacker(EntityPlayer ent) {
        for (EntityPlayer hacker : hackers) {
            if (!ent.getName().equals(hacker.getName())) continue;
            return true;
        }
        return false;
    }

}